FROM denoland/deno:alpine-1.18.1

EXPOSE 8080

RUN deno install -qAn vr https://deno.land/x/velociraptor@1.4.0/cli.ts

WORKDIR /usr/app

# Cache dependencies
COPY server/deps.ts .
RUN deno cache --unstable deps.ts

# Cache app
COPY server .
COPY server/imports.prod.json imports.json
COPY client/src/models types
RUN deno cache --unstable --importmap imports.json app.ts

CMD [ "vr", "start" ]