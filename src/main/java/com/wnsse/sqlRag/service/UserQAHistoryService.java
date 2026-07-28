package com.wnsse.sqlRag.service;

import com.wnsse.sqlRag.entity.UserQAHistory;
import org.springframework.stereotype.Service;

@Service
public interface UserQAHistoryService {

    int insertData(UserQAHistory history, String tableName);
}
