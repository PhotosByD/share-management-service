# share-management-service

## Docker build locally
docker build -t share-service .
docker run -d --name share-service -p 8084:8084 share-service

## Database 
docker run -d --name pg-shares -e POSTGRES_USER=dbuser -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=share -p 5432:5432 postgres:10.5

## ETCD (Ubuntu)
docker run -d -p 2379:2379 --name etcd --volume=/tmp/etcd-data:/etcd-data quay.io/coreos/etcd:latest /usr/local/bin/etcd --name my-etcd-1 --data-dir /etcd-data --listen-client-urls http://0.0.0.0:2379 --advertise-client-urls http://0.0.0.0:2379 --listen-peer-urls http://0.0.0.0:2380 --initial-advertise-peer-urls http://0.0.0.0:2380 --initial-cluster my-etcd-1=http://0.0.0.0:2380 --initial-cluster-token my-etcd-token --initial-cluster-state new --auto-compaction-retention 1 -cors="*"
    