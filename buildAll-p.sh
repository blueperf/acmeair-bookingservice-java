podman build -t ol-book -f Dockerfile-daily . 
podman build -t wf-book -f Dockerfile-wf .
podman build -t pm-book -f Dockerfile-pm .
podman build -t qu-book -f Dockerfile-qu .
podman build -t qn-book -f Dockerfile-qn .
podman build -t tm-book -f Dockerfile-tm .
podman build -t hd-book -f Dockerfile-hd .
podman build -t ol-io-book -f Dockerfile-daily-io --cpuset-cpus="2-3" --cap-add=CHECKPOINT_RESTORE --cap-add=SYS_PTRACE --security-opt seccomp=unconfined .
