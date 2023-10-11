CID=$(podman run -d -p 9080:9080 --memory=1g --cpuset-cpus="2-3" ol-book)
sleep 20
curl localhost:9080/booking/status
podman stop ${CID}
podman rm ${CID}

CID=$(podman run -d -p 9080:9080 --memory=1g --cpuset-cpus="2-3" wf-book)
sleep 20
curl localhost:9080/booking/status
podman stop ${CID}
podman rm ${CID}

CID=$(podman run -d -p 9080:9080 --memory=1g --cpuset-cpus="2-3" pm-book)
sleep 20
curl localhost:9080/booking/status
podman stop ${CID}
podman rm ${CID}

CID=$(podman run -d -p 9080:9080 --memory=1g --cpuset-cpus="2-3" tm-book)
sleep 20
curl localhost:9080/booking/status
podman stop ${CID}
podman rm ${CID}

CID=$(podman run -d -p 9080:9080 --memory=1g --cpuset-cpus="2-3" hd-book)
sleep 20
curl localhost:9080/booking/status
podman stop ${CID}
podman rm ${CID}

CID=$(podman run -d -p 9080:9080 --memory=1g --cpuset-cpus="2-3" qu-book)
sleep 20
curl localhost:9080/booking/status
podman stop ${CID}
podman rm ${CID}

CID=$(podman run -d -p 9080:9080 --memory=1g --cpuset-cpus="2-3" qn-book)
sleep 20
curl localhost:9080/booking/status
podman stop ${CID}
podman rm ${CID}

CID=$(podman run -d -p 9080:9080 --memory=1g --cpuset-cpus="2-3" --cap-add=CHECKPOINT_RESTORE --cap-add=SETPCAP --security-opt seccomp=unconfined ol-io-book)
sleep 20
curl localhost:9080/booking/status
podman stop ${CID}
podman rm ${CID}
