package com.demo.security.spring.controller;

import com.demo.security.spring.DemoAssertions;
import com.demo.security.spring.generate.NoticeDetailsFileGenerator;
import com.demo.security.spring.model.NoticeDetails;
import com.demo.security.spring.repository.NoticeDetailsRepository;
import com.demo.security.spring.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NoticesControllerTest extends AbstractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NoticeDetailsRepository noticesRepository;

    @Autowired
    private NoticeDetailsFileGenerator noticesGenerator;

    @Autowired
    private Faker faker;

    @Autowired
    Environment environment;

    @Test
    void getNotices() throws Exception {
        // confirm empty notices
        final MvcResult result = mockMvc.perform(get(NoticesController.NOTICES_RESOURCE_PATH)).andExpect(status().isOk()).andReturn();
        final List<NoticeDetails> noticeDetails = asNotices(result.getResponse().getContentAsString());
        assertEquals(0, noticeDetails.size());
        // confirm non-empty notices
        final ZonedDateTime now = ZonedDateTime.now();
        final TestData testData = generateTestData(10);
        List<NoticeDetails> flattenedTestData = testData.flattened();
        Collections.shuffle(flattenedTestData);
        noticesRepository.saveAll(flattenedTestData);
        final MvcResult nonEmptyResult = mockMvc.perform(get(NoticesController.NOTICES_RESOURCE_PATH)).andExpect(status().isOk()).andReturn();
        final List<NoticeDetails> nonEmptyNotices = asNotices(nonEmptyResult.getResponse().getContentAsString());
        assertEquals(10, nonEmptyNotices.size());

        for (NoticeDetails notice : nonEmptyNotices) {
            // check for valid data generation
            assertNoticeIsValid(notice);
            assertTrue(notice.isActive(), "Expected notice to be active " + notice);
            // confirm expected result of com.demo.security.spring.repository.NoticeDetailsRepository.getAllActiveNotices
            assertTrue(notice.getEntityStartAndEndDates().getStartDate().isBefore(now));
            assertTrue(notice.getEntityStartAndEndDates().getEndDate().isAfter(now));
            // checking the persist-time setting of the control dates
            DemoAssertions.assertDateIsNowIsh(notice.getControlDates().getCreated());
            DemoAssertions.assertDateIsNowIsh(notice.getControlDates().getLastUpdated());
            // update and check the update-time value of control dates
            final ZonedDateTime originalCreated = notice.getControlDates().getCreated();
            final ZonedDateTime originalUpdated = notice.getControlDates().getLastUpdated();
            final String expectedUpdatedSummary = "An updated summary";
            notice.setNoticeSummary(expectedUpdatedSummary);
            final NoticeDetails updated = noticesRepository.save(notice);
            assertEquals(expectedUpdatedSummary, updated.getNoticeSummary());
            assertEquals(originalCreated, updated.getControlDates().getCreated());
            assertTrue(originalUpdated.isBefore(updated.getControlDates().getLastUpdated()),
                "Expected lastUpdated field to be updated during save of updated notice " + notice);
        }

        List<NoticeDetails> allNotices = new ArrayList<>();
        noticesRepository.findAll().forEach(allNotices::add);
        for (NoticeDetails notice : allNotices) {
            assertNoticeIsValid(notice);
            if (notice.isFuture()) {
                assertTrue(notice.getEntityStartAndEndDates().getStartDate().isAfter(now), "Expected future notice to start in the future " + notice);
            } else if (notice.isPast()) {
                assertTrue(notice.getEntityStartAndEndDates().getEndDate().isBefore(now), "Expected past notice to end before now " + notice);
            } else {
                assertTrue(notice.getEntityStartAndEndDates().getStartDate().isBefore(now)
                    && notice.getEntityStartAndEndDates().getEndDate().isAfter(now), "Expected current active notice to be current " + notice);
            }
        }

        // execute with a user to confirm no difference
        final MvcResult withUserResult = mockMvc.perform(get(NoticesController.NOTICES_RESOURCE_PATH)
                .with(user(faker.internet().username())))
            .andExpect(status().isOk()).andReturn();
        final List<NoticeDetails> userNotices = asNotices(withUserResult.getResponse().getContentAsString());
        assertEquals(10, userNotices.size());
    }

    @Test
    void testCors() throws Exception {
        _testCors(mockMvc, NoticesController.NOTICES_RESOURCE_PATH, null, null, false);
    }

    @Test
    void testNullRepositoryResponse() {
        NoticesController controller = new NoticesController();
        NoticeDetailsRepository mockRepo = mock(NoticeDetailsRepository.class);
        when(mockRepo.getAllActiveNotices()).thenReturn(null);
        controller.setNoticeDetailsRepository(mockRepo);
        assertNull(controller.getNotices());
        verify(mockRepo, times(1)).getAllActiveNotices();
    }

    private List<NoticeDetails> asNotices(String noticesJson) throws IOException {
        try {
            NoticeDetails[] temp = objectMapper.readValue(noticesJson, NoticeDetails[].class);
            return Arrays.stream(temp).toList();
        } catch (IOException e) {
            fail("Failed to deserialize NoticeDetails list", e);
        } catch (Exception e) {
            fail("Failed when creating notices list from response body", e);
        }
        return null;
    }

    private void assertNoticeIsValid(NoticeDetails notice) {
        assertNotNull(notice.getNoticeId());
        assertNotNull(notice.getNoticeSummary());
        assertNotNull(notice.getNoticeDetails());
        assertNotNull(notice.getControlDates().getCreated());
        assertNotNull(notice.getControlDates().getLastUpdated());
        assertNotNull(notice.getEntityStartAndEndDates().getStartDate());
        assertNotNull(notice.getEntityStartAndEndDates().getEndDate());
    }

    private TestData generateTestData(final int numberEachType) {
        TestData testData = TestData.builder().build();
        for (int i = 0; i < numberEachType; i++) {
            testData.addCurrent(noticesGenerator.generateCurrentNotice());
            testData.addFuture(noticesGenerator.generateFutureNotice());
            testData.addPast(noticesGenerator.generatePastNotice());
        }
        return testData;
    }

    @Builder
    @Getter
    private static class TestData {
        private final List<NoticeDetails> currentNotices = new ArrayList<>();

        private final List<NoticeDetails> futureNotices = new ArrayList<>();

        private final List<NoticeDetails> pastNotices = new ArrayList<>();

        public List<NoticeDetails> flattened() {
            List<NoticeDetails> all = new ArrayList<>();
            all.addAll(currentNotices);
            all.addAll(futureNotices);
            all.addAll(pastNotices);
            return all;
        }

        public TestData clear() {
            currentNotices.clear();
            futureNotices.clear();
            pastNotices.clear();
            return this;
        }

        public TestData addCurrent(NoticeDetails notice) {
            currentNotices.add(notice);
            return this;
        }

        public TestData addFuture(NoticeDetails notice) {
            futureNotices.add(notice);
            return this;
        }

        public TestData addPast(NoticeDetails notice) {
            pastNotices.add(notice);
            return this;
        }
    }

}