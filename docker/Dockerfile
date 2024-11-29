FROM ubuntu:22.04

WORKDIR /app

ENV BUNDLE_ALLOW_ROOT=1
ENV TEMP_DIR=/tmp/jekyll_site

RUN mkdir -p $TEMP_DIR

RUN apt-get update && apt-get install -y \
    ruby-full \
    build-essential \
    zlib1g-dev \
    git && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

RUN gem install jekyll bundler

COPY docker-entrypoint.sh /docker-entrypoint.sh
COPY Gemfile .
RUN bundle config set path $TEMP_DIR/bundle
RUN bundle install

RUN chmod +x /docker-entrypoint.sh

ENTRYPOINT ["sh", "/docker-entrypoint.sh"]
