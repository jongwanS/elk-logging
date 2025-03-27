package com.jw.study.elklogging.domain;

import java.util.List;

public interface ShortenUrlRepository {
    void saveShortenUrl(ShortenUrl shortenUrl);
    ShortenUrl findShortenUrlByShortenUrlKey(String shortenUrlKey);

    List<ShortenUrl> findAll();
}
