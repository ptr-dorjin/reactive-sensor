FROM node:12-alpine
ENV NODE_ENV=production

RUN yarn global add serve

WORKDIR /app
COPY . .
RUN yarn install --production
RUN yarn build

# Documenting ports to be exposed when running in Docker
EXPOSE 5000

CMD ["serve", "-s", "build"]
