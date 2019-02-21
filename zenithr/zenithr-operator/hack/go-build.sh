#!/bin/sh

go generate ./...
if [[ -z ${CI} ]]; then
    source hack/go-test.sh
    operator-sdk build quai.io/bmozaffa/zenithr-operator:latest
    if [[ ${1} == "rhel" ]]; then
        echo "Building Docker image quai.io/bmozaffa/zenithr-operator:latest"
        docker build . -f build/Dockerfile.rhel -t quai.io/bmozaffa/zenithr-operator:latest
    fi
else
    CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -a -o build/_output/bin/zenithr-operator github.com/bmozaffa/zenithr-operator/cmd/manager
fi
