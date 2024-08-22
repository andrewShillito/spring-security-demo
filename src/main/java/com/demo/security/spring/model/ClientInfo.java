package com.demo.security.spring.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import ua_parser.Client;
import ua_parser.Parser;

@Embeddable
@Getter
@Setter
@ToString
@Log4j2
public class ClientInfo {

  @Column(name = "remote_address")
  private String remoteAddress;
  @Column(name = "remote_host")
  private String remoteHost;
  @Column(name = "remote_user")
  private String remoteUser;
  @Column(name = "content_type")
  private String contentType;
  @Column(name = "user_agent_family")
  private String userAgentFamily;
  @Column(name = "user_agent_major")
  private String userAgentMajor;
  @Column(name = "user_agent_minor")
  private String userAgentMinor;
  @Column(name = "user_agent_patch")
  private String userAgentPatch;
  @Column(name = "os_family")
  private String osFamily;
  @Column(name = "os_major")
  private String osMajor;
  @Column(name = "os_minor")
  private String osMinor;
  @Column(name = "os_patch")
  private String osPatch;
  @Column(name = "os_patch_minor")
  private String osPatchMinor;
  @Column(name = "device_family")
  private String deviceFamily;

  public static ClientInfo fromRequest(@NonNull HttpServletRequest request) {
    final ClientInfo clientInfo = new ClientInfo();
    final String userAgentHeader = request.getHeader(HttpHeaders.USER_AGENT);
    if (StringUtils.isBlank(userAgentHeader)) {
      log.error(() -> HttpHeaders.USER_AGENT + " header is null for request " + request.getRequestURI());
    } else {
      Parser uaParser = new Parser();
      Client client = uaParser.parse(userAgentHeader);

      clientInfo.setDeviceFamily(client.device.family);

      clientInfo.setUserAgentFamily(client.userAgent.family);
      clientInfo.setUserAgentMajor(client.userAgent.major);
      clientInfo.setUserAgentMinor(client.userAgent.minor);
      clientInfo.setUserAgentPatch(client.userAgent.patch);

      clientInfo.setOsFamily(client.os.family);
      clientInfo.setOsMajor(client.os.major);
      clientInfo.setOsMinor(client.os.minor);
      clientInfo.setOsPatch(client.os.patch);
      clientInfo.setOsPatchMinor(client.os.patchMinor);
    }
    clientInfo.setRemoteAddress(request.getRemoteAddr());
    clientInfo.setRemoteHost(request.getRemoteHost());
    clientInfo.setRemoteUser(request.getRemoteUser()); // can be null if not authenticated
    clientInfo.setContentType(request.getContentType());
    return clientInfo;
  }
}
