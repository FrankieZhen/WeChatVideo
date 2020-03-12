package com.xcz.mapper;

import com.xcz.pojo.SearchRecords;
import com.xcz.utils.MyMapper;

import java.util.List;

public interface SearchRecordsMapper extends MyMapper<SearchRecords> {
	
	public List<String> getHotwords();
}