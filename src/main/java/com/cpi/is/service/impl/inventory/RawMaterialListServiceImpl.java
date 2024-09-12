package com.cpi.is.service.impl.inventory;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;
import com.cpi.is.dao.impl.inventory.RawMaterialListDAOImpl;
import com.cpi.is.dao.impl.maintenance.RawMaterialDAOImpl;
import com.cpi.is.dao.maintenance.RawMaterialDAO;
import com.cpi.is.entity.UserEntity;
import com.cpi.is.entity.inventory.RawMaterialListEntity;
import com.cpi.is.exception.InvalidJsonException;
import com.cpi.is.service.inventory.RawMaterialListService;
import com.cpi.is.validation.JsonValidate;
import com.cpi.is.util.ValidationUtil;

public class RawMaterialListServiceImpl implements RawMaterialListService, JsonValidate{
	
	private RawMaterialListDAOImpl rawMaterialListDAO = new RawMaterialListDAOImpl();
	private RawMaterialDAOImpl rawMaterialDao = new RawMaterialDAOImpl();
	
	private RawMaterialListEntity jsonToEntity(JSONObject json) throws NumberFormatException, JSONException, Exception {
		return new RawMaterialListEntity(
				json.getInt("materialListId"),
				json.getString("materialCode"),
				json.getInt("quantity"),
				json.getInt("userId"),
				convertStringToSqlDate(json.getString("dateReceive")),
				json.getInt("branchId"));
	}
	
	
	
	public RawMaterialListDAOImpl getRawMaterialListDAO() {
		return rawMaterialListDAO;
	}



	public void setRawMaterialListDAO(RawMaterialListDAOImpl rawMaterialListDAO) {
		this.rawMaterialListDAO = rawMaterialListDAO;
	}



	private static Date convertStringToSqlDate(String dateString) {
        // Define the format of the input string
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        try {
            // Parse the input string into a java.util.Date
            java.util.Date utilDate = inputFormat.parse(dateString);
            
            // Convert java.util.Date to java.sql.Date
            return new Date(utilDate.getTime());
        } catch (ParseException e) {
            // Handle parsing exception
            e.printStackTrace();
            return null;
        }
    }
	
	public List<RawMaterialListEntity>  getRawMaterialList(HttpServletRequest request) throws Exception {
		HttpSession session = request.getSession();
		UserEntity user = (UserEntity) session.getAttribute("user");
		List<RawMaterialListEntity> rawMaterialLists = rawMaterialListDAO.getRawMaterialList(user.getBranchId());
        for (RawMaterialListEntity entity : rawMaterialLists) {
            UserEntity rawMaterialUser = entity.getUser();
            if (rawMaterialUser != null) {
            	rawMaterialUser.setPassword("");
            }
        }
		return rawMaterialLists;
	}



	@Override
	public String saveRawMaterial(HttpServletRequest request, HttpSession session) throws Exception {
		JSONObject json = new JSONObject(request.getParameter("item"));
		UserEntity user = (UserEntity) session.getAttribute("user");
		json.put("userId", user.getUserId());
		json.put("branchId", user.getBranchId());
		validateJson(json);
		return rawMaterialListDAO.saveRawMaterial(
				jsonToEntity(json));
		//return null;		
	}

	@Override
	public String deleteRawMaterial(HttpServletRequest request) throws Exception {
		System.out.println(request.getParameter("item"));
		return rawMaterialListDAO.deleteRawMaterial(
				jsonToEntity(new JSONObject(request.getParameter("item"))));
	}



	@Override
	public void validateJson(JSONObject jsonObject) throws InvalidJsonException {

		String requiredFields[] = {"materialListId", "materialCode", "quantity", "userId", "dateReceive", "branchId"};
	    ValidationUtil.checkFields(requiredFields, jsonObject);

	    ValidationUtil.isValidDate(jsonObject.getString("dateReceive"));
	    
	    // Validate quantity
	    String quantityStr = jsonObject.get("quantity").toString();
	    
	    ValidationUtil.checkNumber(quantityStr);
	    
	    checkForeignKey(jsonObject.getString("materialCode"));
	    	
	}

	public boolean checkForeignKey (String jsonObject) throws InvalidJsonException {
		try {
			if (rawMaterialDao.getRawMaterialById(jsonObject) == null) {
				throw new InvalidJsonException("Invalid Parameter");
			}
		} catch (Exception e) {
			if (e instanceof InvalidJsonException) {
				throw (InvalidJsonException) e;
			}
			e.printStackTrace();
		}
		return true;
	}
}