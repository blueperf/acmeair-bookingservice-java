docker build -t ol-auth .
docker build -t wf-auth -f Dockerfile-wf .
docker build -t qu-auth -f Dockerfile-qu .
docker build -t qn-auth -f Dockerfile-qn .
docker build -t pm-auth -f Dockerfile-pm .

