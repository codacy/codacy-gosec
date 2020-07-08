FROM golang:1.14.4-alpine3.11 as builder

COPY doc-generation /doc-generation

WORKDIR /doc-generation
RUN mkdir -p /docs/description
RUN go run main.go -docFolder=../docs

FROM alpine:3.11

COPY --from=builder /docs /docs
COPY entry.sh /

RUN adduser -u 2004 -D docker && chown -R docker:docker /docs

USER docker

ENTRYPOINT [ "sh", "/entry.sh" ]
