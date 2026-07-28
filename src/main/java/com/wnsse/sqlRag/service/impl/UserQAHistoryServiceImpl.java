package com.wnsse.sqlRag.service.impl;

import com.wnsse.sqlRag.entity.UserQAHistory;
import com.wnsse.sqlRag.mapper.UserQAHistoryMapper;
import com.wnsse.sqlRag.service.UserQAHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserQAHistoryServiceImpl implements UserQAHistoryService {

    private final UserQAHistoryMapper historyMapper;
    @Override
    public int insertData(UserQAHistory history, String tableName) {
        if(history == null){
            return 0;
        }
        return historyMapper.insertData(history, tableName);
    }
}
