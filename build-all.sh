podman build -t ol-book -f Dockerfile-ol . 
podman build -t wl-book -f Dockerfile-wl .
podman build -t wf-book -f Dockerfile-wf .
podman build -t pm-book -f Dockerfile-pm .
podman build -t qu-book -f Dockerfile-qu .
podman build -t qn-book -f Dockerfile-qn .
podman build -t tm-book -f Dockerfile-tm .
podman build -t hd-book -f Dockerfile-hd .
podman build -t hd4-book -f Dockerfile-hd4 .
podman build -t ol-io-book -f Dockerfile-io --cpu-quota=200000 -m 1g --cap-add=CHECKPOINT_RESTORE --cap-add=SYS_PTRACE --security-opt seccomp=unconfined .

sed -i "s@<feature>microProfile-7.0</feature>@<feature>microProfile-6.1</feature>@" src/main/liberty/config/server.xml
podman build -t old-book -f Dockerfile-old
sed -i "s@<feature>microProfile-6.1</feature>@<feature>microProfile-7.0</feature>@" src/main/liberty/config/server.xml
