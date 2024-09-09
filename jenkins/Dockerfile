FROM jenkins/jenkins:2.475-jdk21
USER root
RUN apt-get update && apt-get install -y lsb-release
RUN curl -fsSLo /usr/share/keyrings/docker-archive-keyring.asc \
  https://download.docker.com/linux/debian/gpg
RUN echo "deb [arch=$(dpkg --print-architecture) \
  signed-by=/usr/share/keyrings/docker-archive-keyring.asc] \
  https://download.docker.com/linux/debian \
  $(lsb_release -cs) stable" > /etc/apt/sources.list.d/docker.list
RUN apt-get update && apt-get install -y docker-ce-cli
USER jenkins
RUN jenkins-plugin-cli --plugins dashboard-view:2.517.v776a_b_811a_b_4e cloudbees-folder:6.942.vb_43318a_156b_2 \
    antisamy-markup-formatter:162.v0e6ec0fcfcf6 build-timeout:1.33 credentials-binding:681.vf91669a_32e45 timestamper:1.27 \
    ws-cleanup:0.46 ant:511.v0a_a_1a_334f41b_ gradle:2.12.1 workflow-aggregator:600.vb_57cdd26fdd7 \
    github-branch-source:1797.v86fdb_4d57d43 pipeline-github-lib:61.v629f2cc41d83 pipeline-graph-view:335.v064a_314706a_e \
    git:5.4.1 github:1.40.0 ssh-slaves:2.973.v0fa_8c0dea_f9f matrix-auth:3.2.2 pam-auth:1.11 ldap:733.vd3700c27b_043 \
    email-ext:1814.v404722f34263 emailext-template:1.5 dark-theme:479.v661b_1b_911c01 blueocean:1.27.14 \
    docker-workflow:580.vc0c340686b_54