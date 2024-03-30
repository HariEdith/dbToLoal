package com.fingress.kmb.function.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Base64Utils;

import com.fingress.domain.common.AppContextAccessor;
import com.fingress.domain.service.FgQueryRequest;
import com.fingress.domain.service.FgQueryResponse;
import com.fingress.kmb.services.FgKMBServiceConstant;
import com.fingress.service.function.FgFunctionService;
import com.fingress.service.query.FgBaseQueryService;

public class FgFileDownload implements FgFunctionService {

	static final Log LOG = LogFactory.getLog(FgFileDownload.class);

	@Override
	@SuppressWarnings("unchecked")
	public Object apply(Object request) {

		String methodName = "apply -";
		LOG.info(methodName + " Start");
		Map<String, Object> dataMap = new HashMap<>();
		Map<String, Object> fileAttachData = new HashMap<>();
		Map<String, Object> response = new HashMap<>();
		Map<String, Object> eventData = new HashMap<>();
		try {
			Map<String, Object> requestMap = (Map<String, Object>) request;
			if (requestMap.get(FgKMBServiceConstant.DATA_KEY) instanceof List) {
				List<Map<String, Object>> dataList = (List<Map<String, Object>>) requestMap
						.get(FgKMBServiceConstant.DATA_KEY);
				dataMap = dataList.get(0);
			} else {
				dataMap = (Map<String, Object>) requestMap.get(FgKMBServiceConstant.DATA_KEY);
			}
			String referenceId = (String) dataMap.get(FgKMBServiceConstant.REFERENCE_ID);
			if (StringUtils.isNotEmpty(referenceId)) {
				eventData = getEventData(referenceId, FgKMBServiceConstant.FG_EVENTS, FgKMBServiceConstant.EVENTS);
				String fileAttachRef = (String) eventData.get(FgKMBServiceConstant.FILE_ATTACH_REF_NO);
				if (StringUtils.isNotEmpty(fileAttachRef)) {
					fileAttachData = getEventData(fileAttachRef, FgKMBServiceConstant.FG_FILE_CONTENT,
							FgKMBServiceConstant.FILE_CONTENT);
				}
			}
			if (fileAttachData == null || fileAttachData.isEmpty()
					|| String.valueOf(fileAttachData.get(FgKMBServiceConstant.FILE_ATTACHMENT)).isEmpty()) {
				LOG.error(methodName + "File Attachment is Empty");
				response.put(FgKMBServiceConstant.STATUS, FgKMBServiceConstant.ERROR);
				response.put(FgKMBServiceConstant.MESSAGE, "No Record found,Unable to Download");
				response.put(FgKMBServiceConstant.ERROR_DES, "File Attachment is Empty");
				return response;
			}

			String fileType = (String) fileAttachData.get(FgKMBServiceConstant.FILE_TYPE);
			String attachment = (String) fileAttachData.get(FgKMBServiceConstant.FILE_ATTACHMENT);
			String fileName = (String) eventData.get(FgKMBServiceConstant.FILE_NAME);
			fileName = FilenameUtils.removeExtension(fileName);
			byte[] content = attachment.getBytes();
			if (FgKMBServiceConstant.XLS.equalsIgnoreCase(fileType)
					|| FgKMBServiceConstant.XLSX.equalsIgnoreCase(fileType)) {
				content = Base64Utils.decodeFromString(attachment);
			}
			String documentName = fileName + "." + fileType;
			response.put(FgKMBServiceConstant.DOC_CONTENT, content);
			response.put(FgKMBServiceConstant.STATUS, FgKMBServiceConstant.SUCCESS);
			response.put(FgKMBServiceConstant.MESSAGE, "File downloaded successfully");
			response.put("requestCategory", "Download");
			response.put("documentName", documentName);
			response.put("documentMimeType", fileType);
			response.put("documentContent", content);
			response.put("applicationType", fileType);
			response.put("documentExt", FilenameUtils.getExtension(referenceId));

		} catch (Exception e) {
			LOG.error("Can't downloaded " + e.getMessage(), e);
		}
		LOG.info(methodName + " End");
		return response;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getEventData(String referenceId, String typeCode, String subTypeCode) {
		Map<String, Object> map = new HashMap<>();
		FgQueryRequest queryContext = new FgQueryRequest();
		HashMap<String, Object> filterClause = new HashMap<>();
		filterClause.put(FgKMBServiceConstant.TYPE_CODE, typeCode);
		filterClause.put(FgKMBServiceConstant.SUB_TYPE_CODE, subTypeCode);
		filterClause.put(FgKMBServiceConstant.REFERENCE_ID, referenceId);
		queryContext.setFilterClause(filterClause);
		FgBaseQueryService fgBaseQueryService = AppContextAccessor.getBean(FgKMBServiceConstant.FG_BASE_QUERY_SERVICE);
		FgQueryResponse queryResponse = fgBaseQueryService.queryServiceApi(queryContext);

		if (queryResponse.getEntityData() instanceof HashMap) {
			map = (Map<String, Object>) queryResponse.getEntityData();
		} else if (queryResponse.getEntityData() instanceof List) {
			List<Map<String, Object>> responseList = (List<Map<String, Object>>) queryResponse.getEntityData();
			if (!responseList.isEmpty())
				map = responseList.get(0);
		}
		return map;
	}
}
