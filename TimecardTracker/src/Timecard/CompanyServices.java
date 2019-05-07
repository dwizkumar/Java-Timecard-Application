package Timecard;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.*;
import javax.ws.rs.*;

import org.json.JSONObject;

import companydata.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * CompanyServices class exposes Restful APIs with 4 HTTP methods: GET, POST, PUT, and DELETE of 
 * department, company, and time-tracker functionality
 *   
 * @author Dwiz Kumar
 * 
 */

@Path("CompanyServices")
public class CompanyServices {

	DataLayer dl = null;
    private static final String environment = "development";	
    private static final String companyId = "dxk3754";
    
    /**
     * Delete company: Method deletes all Department, Employee, Timecard records of requested company 
     * @param company
     * @return successful deletion of records. Return record does not exist message if requesting the same
     * company for the second time    
     * 
     */
    
	@Path("company")
	@DELETE
	@Produces("application/json")
	public Response deleteCompany(
			@DefaultValue(companyId) @QueryParam("company") String company){
	 
		try{
			dl = new DataLayer(environment);	
			if(company.trim().isEmpty()){
			   return Response.ok("{\"error\": \"company should not be empty.\"}").build();	
			}else{
				
			    int rowsDeleted = dl.deleteCompany(company);
			    if(rowsDeleted >= 1){
			        return Response.ok("{\"success\": \"companyName's information deleted.\"}").build();
			    }else{
			    	return Response.ok("{\"error\": \"companyName's does not exist.\"}").build();
			    }
			}
		}
		catch(Exception e){
			  return Response.ok("{\"error\":\"" + e.getMessage() + "\"}").build();
		}
		finally{
			dl.close();
		}
	}  
	
	
	/**
	 * Get Department: Method returns particular Department for the requested company and department id 
	 * @param company
	 * @param dept_id
	 * @return returns the requested Department
	 *  
	 */
	
	@Path("department")
	@GET
	@Produces("application/json")
	public Response getDepartment(
			@DefaultValue(companyId) @QueryParam("company") String company,
			@QueryParam("dept_id") int dept_id){
		try{
			dl = new DataLayer(environment);	
			if(company.trim().isEmpty()){
			  return Response.ok("{\"error\": \"company should not be empty.\"}").build();	
			}
			if(dept_id == 0){
			  return Response.ok("{\"error\": \"dept_id should not be empty.\"}").build();
			}else{
			    Department dept = dl.getDepartment(company, dept_id);
			    if(dept == null){
			    	return Response.ok("{\"error\": \"No record found for the request.\"}").build();
			    }
			    Gson gson = new Gson();
			    return Response.ok(gson.toJson(dept, Department.class)).build();
			}		
  		}
		catch(Exception e){
			return Response.ok("{\"error\":\"" + e.getMessage() + "\"}").build();
		}
		finally{
			dl.close();
		}
	}
	
	
	/**
	 * Get all Departments: Method returns all departments for the requested company
	 * @param company
	 * @return the requested list of Departments
	 * 
	 */
	
	@Path("departments")
	@GET
	@Produces("application/json")
	public Response getDepartments(
			@DefaultValue(companyId) @QueryParam("company") String company){
		try{
			dl = new DataLayer(environment);
			if(company.trim().isEmpty()){
			  return Response.ok("{\"error\": \"company should not be empty.\"}").build();	
			}else{
			    List<Department> depts = dl.getAllDepartment(company);
			    if(depts.isEmpty()){
			    	return Response.ok("{\"error\": \"No record found for the request.\"}").build();
			    }
				Gson gson = new Gson();
				return Response.ok(gson.toJson(depts)).build();
			}
		}
		catch(Exception e){
			return Response.ok("{\"error\":\"" + e.getMessage() + "\"}").build();
		}
		finally{
			dl.close();
		}
	}
	
	
	
	/**
	 * Insert a Department: Method inserts a new Department record and return the inserted Department record   
	 * @param jsonReq: company, dept_name, dept_no, and location 
	 * @return inserted record
	 * 
	 */
	
	@Path("department")
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public Response insertDepartment(String jsonReq){
		try{
			dl = new DataLayer(environment);
			Gson gson = new Gson();
			Department department = gson.fromJson(jsonReq, Department.class);
			// Empty check for all requested field
			if(department.getCompany().trim().isEmpty()){
			  return Response.ok("{\"error\": \"company should not be empty.\"}").build();	
			}
			if(department.getDeptName().trim().isEmpty()){
			  return Response.ok("{\"error\": \"dept_name should not be empty.\"}").build();	
			}
			if(department.getDeptNo().trim().isEmpty()){
			  return Response.ok("{\"error\": \"dept_no should not be empty.\"}").build();	
			}
			if(department.getLocation().trim().isEmpty()){
			  return Response.ok("{\"error\": \"location should not be empty.\"}").build();	
			}else{
			     
				List<Department> getAllDept = dl.getAllDepartment(department.getCompany());
				for(Department d: getAllDept){
					// check for the duplicate dept no.
					if(d.getDeptNo().equals(department.getDeptNo())){
						return Response.ok("{\"error\": \"Duplicate  dept no. found\"}").build();
					}
				}
				Department dept = new Department(department.getCompany(),department.getDeptName(),department.getDeptNo(),department.getLocation());	
			    dept = dl.insertDepartment(dept);
			    String json = gson.toJson(dept, Department.class);
			    return Response.ok(json).build();
			} 
		}
		catch(Exception e){
			return Response.ok("{\"error\":\"" + e.getMessage() + "\"}").build();
		}
		finally{
			dl.close();
		}
	}
	
	
	/**
	 * Update a Department: Method updates existing Department record and returns updated record
	 * @param dept_id
	 * @param company
	 * @param dept_name
	 * @param dept_no
	 * @param location
	 * @return updated record
	 * 
	 */
	
	@Path("department")
	@PUT
	@Produces("application/json")
	@Consumes("application/x-www-form-urlencoded")
	public Response updateDepartment(@FormParam("dept_id") int dept_id,
			@FormParam("company") String company,
			@FormParam("dept_name") String dept_name,
			@FormParam("dept_no") String dept_no,
			@FormParam("location") String location){
		try{

			dl = new DataLayer(environment);
			Gson gson = new Gson();
			if(dept_id == 0){
			  return Response.ok("{\"error\": \"dept_id should not be empty.\"}").build();	
			}
			if(company.trim().isEmpty()){
			  return Response.ok("{\"error\": \"company should not be empty.\"}").build();	
			}
			if(dept_name.trim().isEmpty()){
			  return Response.ok("{\"error\": \"dept_name should not be empty.\"}").build();	
			}
			if(dept_no.trim().isEmpty()){
			  return Response.ok("{\"error\": \"dept_no should not be empty.\"}").build();	
			}
			if(location.trim().isEmpty()){
			  return Response.ok("{\"error\": \"location should not be empty.\"}").build();	
			}else{
			    
				Department department = dl.getDepartment(company, dept_id);
				List<Department> getAllDept = dl.getAllDepartment(company);
				
				for(Department d: getAllDept){
					// check for the duplicate dept no.
					if(d.getDeptNo().equals(dept_no)){
						return Response.ok("{\"error\": \"Duplicate  dept no. found\"}").build();
					}
				}
				if(department == null){
					return Response.ok("{\"error\": \"No matching dept_id found\"}").build();
				}
		
				department.setCompany(company);
				department.setDeptName(dept_name);
				department.setDeptNo(dept_no);
				department.setLocation(location);
				
				department = dl.updateDepartment(department);
			    String json = gson.toJson(department, Department.class);
			    return Response.ok(json).build();
			}
		}
		catch(Exception e){
			return Response.ok("{\"error\":\"" + e.getMessage() + "\"}").build();
		}
		finally{
			dl.close();
		}
	}
	
	
	/**
	 * Delete a Department: Method deletes a Department of a requested company and Department id 
	 * @param company
	 * @param dept_id
	 * @return the number of rows deleted
	 * 
	 */
	
	@Path("department")
	@DELETE
	@Produces("application/json")
	public Response deleteDepartment(
		@DefaultValue(companyId) @QueryParam("company") String company,
		@QueryParam("dept_id") int dept_id){
		 
		try{
			dl = new DataLayer(environment);	
			if(company.trim().isEmpty()){
			  return Response.ok("{\"error\": \"company should not be empty.\"}").build();	
			}
			if(dept_id == 0){
				  return Response.ok("{\"error\": \"dept_id should not be empty.\"}").build();	
			}else{
			     
				List<Employee> emps = dl.getAllEmployee(company);
				if(!emps.isEmpty()){
					for(Employee emp :emps){
						// Delete all Employees for the requested dept_id
					   if(emp.getDeptId() == dept_id){	
							List<Timecard> times = dl.getAllTimecard(emp.getId());
							// Delete all Timecards
							if(!times.isEmpty()){
								for(Timecard time: times){
								   dl.deleteTimecard(time.getId());
								}
							}
							dl.deleteEmployee(emp.getId());
					   }
					}
				}
				// Delete Department
				int rowsDeleted = dl.deleteDepartment(company, dept_id);
			    if(rowsDeleted >= 1){
			        return Response.ok("{\"success\": \"Department "+ dept_id +" from "+ company +" deleted.\"}").build();
			    }else{
			    	return Response.ok("{\"error\": \"Department "+ dept_id +" from "+ company +" does not exist.\"}").build();
			    }
			}
		}
		catch(Exception e){
			  return Response.ok("{\"error\":\"" + e.getMessage() + "\"}").build();
		}
		finally{
			dl.close();
		}
	}
	
	
	/**
	 * Get Employee: Method returns a particular Employee record of the matching Employee id
	 * @param emp_id
	 * @return requested Employee record
	 * 
	 */
	
	@Path("employee")
	@GET
	@Produces("application/json")
	public Response getEmployee(
	    	@QueryParam("emp_id") int emp_id){
		try{
			dl = new DataLayer(environment);
			if(emp_id == 0){
			  return Response.ok("{\"error\": \"emp_id should not be empty.\"}").build();	
			}else{			
					
			    Employee emp = dl.getEmployee(emp_id);
			    
			    if(emp == null){
			    	return Response.ok("{\"error\": \"No record found for the request.\"}").build();
			    }
			    // set the date format to yyyy-MM-dd
			    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
			    return Response.ok(gson.toJson(emp)).build();
			}
		}
		catch(Exception e){
			return Response.ok("{\"error\":\"" + e.getMessage() + "\"}").build();
		}
		finally{
			dl.close();
		}
	}
	
		
	
	/**
	 * Get all Employees: Method returns a list of employees for the specific company 
	 * @param company
	 * @return requested lists of Employees
	 * 
	 */
	
	@Path("employees")
	@GET
	@Produces("application/json")
	public Response getEmployees(
			@DefaultValue(companyId) @QueryParam("company") String company){
		try{
			dl = new DataLayer(environment);
			if(company.trim().isEmpty()){
			  return Response.ok("{\"error\": \"company should not be empty.\"}").build();	
			}else{
			    List<Employee> emps = dl.getAllEmployee(company);
			    
			    if(emps.isEmpty()){
			    	return Response.ok("{\"error\": \"No record found for the request.\"}").build();
			    }
			    // set the date format to yyyy-MM-dd
			    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
			    return Response.ok(gson.toJson(emps)).build();
			}
		}
		catch(Exception e){
			return Response.ok("{\"error\":\"" + e.getMessage() + "\"}").build();
		}
		finally{
			dl.close();
		}
	}
	
	
	/**
	 * Insert an Employee: Method insert a new Employee record and returns the inserted record. 
	 * @param jsonReq: emp_name, emp_no, hire_date, job, salary, dept_id, and mng_id
	 * @return a newly inserted Employee record
	 * 
	 */
	
	@Path("employee")
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public Response insertEmployee(String jsonReq){
		try{
			
			dl = new DataLayer(environment);
			JSONObject jsonObject = new JSONObject(jsonReq);
			String hireDate = jsonObject.getString("hire_date");
			
			// Using regular expression to match the format
			if(hireDate.trim().isEmpty() || !hireDate.matches("\\d{4}-\\d{2}-\\d{2}")){
				return Response.ok("{\"error\": \"hire_date should be in yyyy-MM-dd format.\"}").build(); 	
			}
			
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
			Employee employee = gson.fromJson(jsonReq, Employee.class);
			
			// Validate for empty values
			if(employee.getEmpName().trim().isEmpty()){
			  return Response.ok("{\"error\": \"emp_name should not be empty.\"}").build();	
			}
			if(employee.getEmpNo().trim().isEmpty()){
			  return Response.ok("{\"error\": \"emp_no should not be empty.\"}").build();	
			}
			if(employee.getJob().trim().isEmpty()){
			  return Response.ok("{\"error\": \"job should not be empty.\"}").build();	
			}
			if(employee.getSalary() == 0.0){
			  return Response.ok("{\"error\": \"salary should not be empty.\"}").build();	
			}
			if(employee.getDeptId() == 0){
			  return Response.ok("{\"error\": \"dept_id should not be empty.\"}").build();	
			}else{
				
				Department getDept = dl.getDepartment(companyId, employee.getDeptId());
		    	Employee getEmp = dl.getEmployee(employee.getMngId());
		    	Date hire_date = employee.getHireDate();
		    	Date current_date = new Date();
		    	
		    	// Set the date format to 'E' to retrieve day like Sat, Mon etc.
		    	SimpleDateFormat simpleDateformat = new SimpleDateFormat("E");
		    	String day_of_date = simpleDateformat.format(hire_date).toLowerCase();
		    	List<Employee> allEmp = dl.getAllEmployee(companyId);
		    	
		    	
		    	if(getDept == null){
		    	   return Response.ok("{\"error\": \"No matching dept_Id found\"}").build();
		    	}
		    	// Match mng_Id only if Employee list is not empty and mng_Id not equal to 0
		    	if(!allEmp.isEmpty()){
		    	  if(getEmp == null && employee.getMngId() != 0)	
		    	      return Response.ok("{\"error\": \"No matching employee found for mng_Id\"}").build();
		    	  // Check duplicate emp_no
		    	  for(Employee e: allEmp){
		    		  if(e.getEmpNo().equals(employee.getEmpNo())){
		    			  return Response.ok("{\"error\": \"Duplicate emp_no found\"}").build();
		    		  }
		    	  }
		    	}	
		    	// validate hire_date should be less than or equal to the current_date
		    	if(hire_date.compareTo(current_date)>0){
		    	   return Response.ok("{\"error\": \"hire_date should not be a future date\"}").build();	
		    	}
		    	// hire_date should be a weekday
		    	if(day_of_date.equals("sat") || day_of_date.equals("sun")){
		    		return Response.ok("{\"error\": \"hire_date should not be a saturday or sunday\"}").build();
		    	}else{
					Employee emp = new Employee(employee.getEmpName(),employee.getEmpNo(),new java.sql.Date(hire_date.getTime()),employee.getJob(),employee.getSalary(),employee.getDeptId(),employee.getMngId());
					emp = dl.insertEmployee(emp);
					return Response.ok(gson.toJson(emp)).build();
		    	}
			}
		}
		catch(Exception e){
			return Response.ok("{\"error\":\"" + e.getMessage() + "\"}").build();
		}
		finally{
			dl.close();
		}
	}
	

	/**
	 * Update an Employee: Method updates the existing employee record and returns the updated Employee
	 * @param emp_id
	 * @param emp_name
	 * @param emp_no
	 * @param hire_date
	 * @param job
	 * @param salary
	 * @param dept_id
	 * @param mng_id
	 * @return the updated Employee
	 * 
	 */
	@Path("employee")
	@PUT
	@Produces("application/json")
	@Consumes("application/x-www-form-urlencoded")
	public Response updateEmployee(@FormParam("emp_id") int emp_id,
			@FormParam("emp_name") String emp_name,
			@FormParam("emp_no") String emp_no,
			@FormParam("hire_date") String hire_date,
			@FormParam("job") String job,
			@FormParam("salary") double salary,
			@FormParam("dept_id") int dept_id,
			@FormParam("mng_id") int mng_id){
		try{
			dl = new DataLayer(environment);
			
			if(emp_id == 0){
			  return Response.ok("{\"error\": \"emp_id should not be empty.\"}").build();	
			}
			if(emp_name.trim().isEmpty()){
			  return Response.ok("{\"error\": \"emp_name should not be empty.\"}").build();	
			}
			if(emp_no.trim().isEmpty()){
			  return Response.ok("{\"error\": \"emp_no should not be empty.\"}").build();	
			}
			// Using regular expression to validate hire_date
			if(hire_date.trim().isEmpty() || !hire_date.matches("\\d{4}-\\d{2}-\\d{2}")){
			  return Response.ok("{\"error\": \"hire_date should be in yyyy-MM-dd.\"}").build();	
			}
			if(job.trim().isEmpty()){
			  return Response.ok("{\"error\": \"job should not be empty.\"}").build();	
			}
			if(salary == 0.0){
			  return Response.ok("{\"error\": \"salary should not be empty.\"}").build();	
			}
			if(dept_id == 0){
			  return Response.ok("{\"error\": \"dept_id should not be empty.\"}").build();	
			}
			if(emp_id == mng_id){
			  return Response.ok("{\"error\": \"mng_id should be different than the emp_id.\"}").build();	
			}else{
				
		    	Department getDept = dl.getDepartment(companyId, dept_id);
		    	Employee getEmp = dl.getEmployee(mng_id);
		    	Employee getEmp1 = dl.getEmployee(emp_id);
		    	Date hireDate = new SimpleDateFormat("yyyy-MM-dd").parse(hire_date);
		    	Date current_date = new Date();
		    	SimpleDateFormat simpleDateformat = new SimpleDateFormat("E");
		    	String day_of_date = simpleDateformat.format(hireDate).toLowerCase();
		    	List<Employee> allEmp = dl.getAllEmployee(companyId);
		    	
		    	// No need as the POST operation checks for the duplicate 
		    	/*for(Employee e: allEmp){
					if(e.getId() == emp_id){
						return Response.ok("{\"error\": \"Duplicate  emp_Id found\"}").build();
					}
				}*/
		    	if(getDept == null){
		    	   return Response.ok("{\"error\": \"No matching dept_Id found\"}").build();
		    	}
		    	
		    	// Check for matching mng_Id and emp_Id for non-empty Employee list 
		    	if(!allEmp.isEmpty()){
		    	  if(getEmp == null && mng_id != 0)	
		    	     return Response.ok("{\"error\": \"No matching employee found for mng_Id\"}").build();
		    	  if(getEmp1 == null)	
			    	 return Response.ok("{\"error\": \"No matching employee found for emp_Id\"}").build();
		    	  for(Employee e: allEmp){
		    		  if(e.getEmpNo().equals(emp_no)){
		    			  return Response.ok("{\"error\": \"Duplicate emp_no found\"}").build();
		    		  }
		    	  }
		    	}
		    	// Validate hire_date by comparing it with the current_date
		    	if(hireDate.compareTo(current_date)>0){
		    	   return Response.ok("{\"error\": \"hire_date should not be a future date\"}").build();	
		    	}
		    	// hire_date should be a weekday 
		    	if(day_of_date.equals("sat") || day_of_date.equals("sun")){
		    		return Response.ok("{\"error\": \"hire_date should not be a saturday or sunday\"}").build();
		    	}
		    	else{
						
		    		getEmp1.setEmpName(emp_name);
		    		getEmp1.setEmpNo(emp_no);
		    		getEmp1.setHireDate(new java.sql.Date(hireDate.getTime()));
		    		getEmp1.setJob(job);
		    		getEmp1.setSalary(salary);
		    		getEmp1.setDeptId(dept_id);
		    		getEmp1.setMngId(mng_id);
		    		getEmp1 = dl.updateEmployee(getEmp1);
		    		
					// Set the response date format to yyyy-MM-dd
					Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();
					return Response.ok(gson.toJson(getEmp1)).build();
			    }
			}	
		}
		catch(Exception e){
			return Response.ok("{\"error\":\"" + e.getMessage() + "\"}").build();
		}
		finally{
			dl.close();
		}
	}


	/**
	 * Delete Employee: Method deletes the existing Employee record based on the requested Employee id 
	 * @param emp_id
	 * @return deleted Employee id and displays does not exist message if user tries to delete the 
	 * same Employee id for the second time 
	 * 
	 */
	
	@Path("employee")
	@DELETE
	@Produces("application/json")
	public Response deleteEmployee(@QueryParam("emp_id") int emp_id){
			 
		try{
			dl = new DataLayer(environment);
			if(emp_id == 0){
			  return Response.ok("{\"error\": \"emp_id should not be empty.\"}").build();	
			}
			else{
			  
			  List<Timecard> timecards = dl.getAllTimecard(emp_id);
			  
			  // Delete all Timecards 
			  if(!timecards.isEmpty()){
				  for(Timecard timecard: timecards){
					 dl.deleteTimecard(timecard.getId());
				  }
			  }
			  // Delete Employee
			  int rowsDeleted = dl.deleteEmployee(emp_id);
			  if(rowsDeleted >= 1){
			      return Response.ok("{\"success\": \"Employee "+ emp_id +" deleted.\"}").build();
			  }else{
			      return Response.ok("{\"error\": \"Employee "+ emp_id +" does not exist.\"}").build();
			  }
			} 
		}
		catch(Exception e){
			  return Response.ok("{\"error\":\"" + e.getMessage() + "\"}").build();
		}
		finally{
			dl.close();
		}
	}


	/**
	 * Get Timecard: Method returns a specific Timecard record for the requested Timecard id  
	 * @param timecard_id
	 * @return requested Timecard
	 * 
	 */
	
	@Path("timecard")
	@GET
	@Produces("application/json")
	public Response getTimecard(@QueryParam("timecard_id") int timecard_id){
		try{
    		dl = new DataLayer(environment);
			if(timecard_id == 0){
			  return Response.ok("{\"error\": \"timecard_id should not be empty.\"}").build();	
			}
			else{
	 		  Timecard time = dl.getTimecard(timecard_id);
	 		  if(time == null){
	 			 return Response.ok("{\"error\": \"No record found for the request.\"}").build();
	 		  }
	 		  Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
			  return Response.ok(gson.toJson(time)).build();
			}
		}
		catch(Exception e){
			return Response.ok("{\"error\":\"" + e.getMessage() + "\"}").build();
		}
		finally{
			dl.close();
		}
	}
	
	
	/**
	 * Get all Timecards: Method returns a list of Timecards for the existing Employee id 
	 * @param emp_id
	 * @return requested list of Timecards
	 * 
	 */
	
	@Path("timecards")
	@GET
	@Produces("application/json")
	public Response getTimecards(@QueryParam("emp_id") int emp_id){
		try{
			dl = new DataLayer(environment);
			if(emp_id == 0){
			  return Response.ok("{\"error\": \"emp_id should not be empty.\"}").build();	
			}
			else{
			  List<Timecard> times = dl.getAllTimecard(emp_id);
			  
			  if(times.isEmpty()){
				  return Response.ok("{\"error\": \"No record found for the request.\"}").build();
			  }
			  Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
			  return Response.ok(gson.toJson(times)).build();
			}
		}
		catch(Exception e){
			return Response.ok("{\"error\":\"" + e.getMessage() + "\"}").build();
		}
		finally{
			dl.close();
		}
	}
 

	/**
	 * Insert a Timecard: Method inserts a new record for a Timecard and returns the inserted Timecard  
	 * @param jsonReq: emp_id, start_time, end_time
	 * @return newly inserted Timecard
	 * 
	 */
	
	@Path("timecard")
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	public Response insertTimecard(String jsonReq){
		try{
			dl = new DataLayer(environment);
			JSONObject jsonObject = new JSONObject(jsonReq);
			String startVal = jsonObject.getString("start_time");
			String endVal = jsonObject.getString("end_time");
			
			if(startVal.trim().isEmpty() || !startVal.matches("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}")){
				return Response.ok("{\"error\": \"start_date should be in yyyy-MM-dd HH:mm:ss format.\"}").build(); 	
			}
			if(endVal.trim().isEmpty() || !endVal.matches("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}")){
				return Response.ok("{\"error\": \"end_date should be in yyyy-MM-dd HH:mm:ss format.\"}").build(); 	
			}
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
			Timecard timecard = gson.fromJson(jsonReq, Timecard.class);
			if(timecard.getEmpId() == 0){
			    return Response.ok("{\"error\": \"emp_id should not be empty.\"}").build();	
			}
			else{
				Employee getEmp = dl.getEmployee(timecard.getEmpId());
		    	
				// Initialize start_time, end_time, and current_time
				Date start_time = timecard.getStartTime();
		    	Date end_time = timecard.getEndTime();
		    	Date current_time = new Date();
		    	
		    	// Get list of Timecards
		    	List<Timecard> times = dl.getAllTimecard(timecard.getEmpId());
		    	
		    	 	
		    	SimpleDateFormat simpleDateformat = new SimpleDateFormat("E");
		    	
		    	String day_of_startTime = simpleDateformat.format(start_time).toLowerCase();
		    	String day_of_endTime = simpleDateformat.format(end_time).toLowerCase();
		    	
		    	// Extract hour, minute,and second from start_time and end_time 
		    	Calendar cal_start = Calendar.getInstance();
		    	cal_start.setTime(start_time);
		    	int start_hour = cal_start.get(Calendar.HOUR_OF_DAY);
		    	int start_mins = cal_start.get(Calendar.MINUTE);
		    	int start_sec = cal_start.get(Calendar.SECOND);
		    	
		    	Calendar cal_end = Calendar.getInstance();
		    	cal_end.setTime(end_time);
		    	int end_hour = cal_end.get(Calendar.HOUR_OF_DAY);
		    	int end_mins = cal_end.get(Calendar.MINUTE);
		    	int end_sec = cal_end.get(Calendar.SECOND);
		    	
		    	Calendar cal_cur = Calendar.getInstance();
		    	
		    	// Match the duplicate date
		    	for(Timecard time: times){
		    		cal_cur.setTime(time.getStartTime());
		    		int cur_startDate = cal_cur.get(Calendar.DATE);
		    		int cur_startMonth = cal_cur.get(Calendar.MONTH);
		    		int cur_startYear = cal_cur.get(Calendar.YEAR);
		    		
		    		if((cur_startDate == cal_start.get(Calendar.DATE)) && 
		    		   (cur_startMonth == cal_start.get(Calendar.MONTH)) && 
		    		   (cur_startYear == cal_start.get(Calendar.YEAR))){
		    			  return Response.ok("{\"error\": \"Exiting record with same start day\"}").build();
		    		}
		    	}
		    	
		    	// Get the millisecond difference of the start date and current date 
			    long days_diff = TimeUnit.DAYS.convert((current_time.getTime()-start_time.getTime()), TimeUnit.MILLISECONDS);	
		    	
			    if(getEmp == null){
		    	   return Response.ok("{\"error\": \"No matching emp_Id found\"}").build();
		    	}
			    // check that start date should not be more than a week older 
		    	if(days_diff>7){
		    	   return Response.ok("{\"error\": \"start_time should be upto a week older than current date\"}").build();	
			   	}
		    	// check that end_time should be at least an hour greater than start date and should be on same date 
		    	if((end_time.getTime()-start_time.getTime())<3600000 
		    			|| cal_end.get(Calendar.DATE) != cal_start.get(Calendar.DATE)
		    			|| cal_end.get(Calendar.MONTH) != cal_start.get(Calendar.MONTH)
		    			|| cal_end.get(Calendar.YEAR) != cal_start.get(Calendar.YEAR)){
		    	   
		    		return Response.ok("{\"error\": \"end_time should be atleast 1 hour greater than the start_time on same day\"}").build();	
		    	}
		    	//Future date validation
		    	if(start_time.compareTo(current_time)>0){
			    	   return Response.ok("{\"error\": \"start_time should not be a future date\"}").build();	
			    }
			   	if(day_of_startTime.equals("sat") || 
			   	   day_of_startTime.equals("sun") || 
			   	   day_of_endTime.equals("sat") || 
			   	   day_of_endTime.equals("sun") ){
			   		
			   		return Response.ok("{\"error\": \"start_time and end_time should not be a saturday or sunday\"}").build();
			   	}
			   	// check that start_time and end_time are in between 6AM to 6PM
			   	if((start_hour < 6) || 
			   	   (start_hour >= 18 && !(start_hour == 18 && start_mins == 0 && start_sec == 0)) || 
			   	   (end_hour < 6) || 
			   	   (end_hour >= 18 && !(end_hour == 18 && end_mins == 0 && end_sec == 0))){
			   		return Response.ok("{\"error\": \"start_time and end_time should be in between 06:00:00 and 18:00:00 inclusive\"}").build();
			   	}
			   	else{
			   		Timestamp startTime = new Timestamp(start_time.getTime());
			   		Timestamp endTime = new Timestamp(end_time.getTime());
				    Timecard time = new Timecard(startTime,endTime, timecard.getEmpId());
					time = dl.insertTimecard(time);
				    return Response.ok(gson.toJson(time)).build();
		    	}
			}
		}
		catch(Exception e){
			return Response.ok("{\"error\":\"" + e.getMessage() + "\"}").build();
		}
		finally{
			dl.close();
		}
	}

	/**
	 * Update Timecard: Method updates the existing Timecard details and returns the updated Timecard 
	 * @param timecard_id
	 * @param emp_id
	 * @param start_time
	 * @param end_time
	 * @return updated Timecard
	 * 
	 */
	
	@Path("timecard")
	@PUT
	@Produces("application/json")
	@Consumes("application/x-www-form-urlencoded")
	public Response updateTimecard(@FormParam("timecard_id") int timecard_id,
			@FormParam("emp_id") int emp_id,
			@FormParam("start_time") String start_time,
			@FormParam("end_time") String end_time){
		try{
			dl = new DataLayer(environment);
			List<Timecard> times = dl.getAllTimecard(emp_id);
			
			if(timecard_id == 0){
			    return Response.ok("{\"error\": \"timecard_id should not be empty.\"}").build();	
			}
			if(emp_id == 0){
			    return Response.ok("{\"error\": \"emp_id should not be empty.\"}").build();	
			}
			if(start_time.trim().isEmpty() || !start_time.matches("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}")){
			    return Response.ok("{\"error\": \"start_date should be in yyyy-MM-dd HH:mm:ss format.\"}").build();	
			}
			if(end_time.trim().isEmpty() || !end_time.matches("\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}")){
			    return Response.ok("{\"error\": \"end_date should be in yyyy-MM-dd HH:mm:ss format.\"}").build();	
			}
			else{
				Employee getEmp = dl.getEmployee(emp_id);
				
				// Store startTime, endTime, and current_time
		    	Date startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(start_time);
		    	Date endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(end_time);
		    	Date current_time = new Date();
		    	
		    	SimpleDateFormat simpleDateformat = new SimpleDateFormat("E");
		    	String day_of_startTime = simpleDateformat.format(startTime).toLowerCase();
		    	String day_of_endTime = simpleDateformat.format(endTime).toLowerCase();
	
		    	// Extract hour, minute and second from start_time and end_time
		    	Calendar cal_start = Calendar.getInstance();
		    	cal_start.setTime(startTime);
		    	int start_hour = cal_start.get(Calendar.HOUR_OF_DAY);
		    	int start_mins = cal_start.get(Calendar.MINUTE);
		    	int start_sec = cal_start.get(Calendar.SECOND);
		    	
		    	Calendar cal_end = Calendar.getInstance();
		    	cal_end.setTime(endTime);
		    	int end_hour = cal_end.get(Calendar.HOUR_OF_DAY);
		    	int end_mins = cal_end.get(Calendar.MINUTE);
		    	int end_sec = cal_end.get(Calendar.SECOND);
			    
		    	Calendar cal_cur = Calendar.getInstance();
		    	
		    	for(Timecard time: times){
		    		cal_cur.setTime(time.getStartTime());
		    		int cur_startDate = cal_cur.get(Calendar.DATE);
		    		int cur_startMonth = cal_cur.get(Calendar.MONTH);
		    		int cur_startYear = cal_cur.get(Calendar.YEAR);
		    		
		    		if((cur_startDate == cal_start.get(Calendar.DATE)) && 
		    		   (cur_startMonth == cal_start.get(Calendar.MONTH)) && 
		    		   (cur_startYear == cal_start.get(Calendar.YEAR))){
		    			return Response.ok("{\"error\": \"Exiting record with same start day\"}").build();
		    		}
		    	}
		    	
			    long days_diff = TimeUnit.DAYS.convert((current_time.getTime()-startTime.getTime()), TimeUnit.MILLISECONDS);	
		    	if(getEmp == null){
		    	   return Response.ok("{\"error\": \"No matching emp_Id found\"}").build();
		    	}
		    	// Check that current date is not more than 7 days after of start_time 
		    	if(days_diff>7){
		    	   return Response.ok("{\"error\": \"start_time should be upto a week older than current date\"}").build();	
			   	}
		    	if((endTime.getTime()-startTime.getTime())<3600000 || 
		    		cal_end.get(Calendar.DATE) != cal_start.get(Calendar.DATE)|| 
		    		cal_end.get(Calendar.MONTH) != cal_start.get(Calendar.MONTH) || 
		    		cal_end.get(Calendar.YEAR) != cal_start.get(Calendar.YEAR)){
		    	   
		    		return Response.ok("{\"error\": \"end_time should be atleast 1 hour greater than the start_time on same day\"}").build();	
		    	}
		    	// Future date validation
		    	if(startTime.compareTo(current_time)>0){
			    	   return Response.ok("{\"error\": \"start_time should not be a future date\"}").build();	
			    }
		    	// Check for weekends
			   	if(day_of_startTime.equals("sat") ||
			   	   day_of_startTime.equals("sun") || 
			   	   day_of_endTime.equals("sat") || 
			   	   day_of_endTime.equals("sun") ){
			   		
			   		 return Response.ok("{\"error\": \"start_time and end_time should not be a saturday or sunday\"}").build();
			   	}
			   	// Check that start_time and end_time should be between 6AM to 6PM 
			   	if((start_hour < 6) || 
			   	   (start_hour >= 18 && !(start_hour == 18 && start_mins == 0 && start_sec == 0)) || 
			   	   (end_hour < 6) || 
			   	   (end_hour >= 18 && !(end_hour == 18 && end_mins == 0 && end_sec == 0))){
			   		return Response.ok("{\"error\": \"start_time and end_time should be in between 06:00:00 and 18:00:00 inclusive\"}").build();
			   	}
			   	else{
			   		Timestamp startTimeStamp = new Timestamp(startTime.getTime());
			   		Timestamp endTimeStamp = new Timestamp(endTime.getTime());
				    
			   		for(Timecard t:times){
						if(t.getId()==timecard_id){
							Timecard timecard = dl.getTimecard(timecard_id);
							timecard.setEmpId(emp_id);
							timecard.setStartTime(startTimeStamp);
							timecard.setEndTime(endTimeStamp);
							
							timecard = dl.updateTimecard(timecard);
							// Set date to yyyy-MM-dd HH:mm:ss format
							Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
						    return Response.ok(gson.toJson(timecard)).build();	
						    
						}
			   		}
					return Response.ok("{\"error\": \"No matching timecard_id " + timecard_id + " found\"}").build();
		    	}
			}	
		}
		catch(Exception e){
			return Response.ok("{\"error\":\"" + e.getMessage() + "\"}").build();
		}
		finally{
			dl.close();
		}
	}

	
	/**
	 * Delete Timecard: Method deletes Timecard records for the requested timecard_id 
	 * @param timecard_id
	 * @return successful deletion message of the existing timecard_id. Displays does not exist message
	 * for the deleted timecard_id if requested for second time deletion
	 * 
	 */
	
	@Path("timecard")
	@DELETE
	@Produces("application/json")
	public Response deleteTimecard(@QueryParam("timecard_id") int timecard_id){
			 
		try{
			dl = new DataLayer(environment);
			if(timecard_id == 0){
			  return Response.ok("{\"error\": \"timecard_id should not be empty.\"}").build();	
			}
			else{
			  int rowsDeleted = dl.deleteTimecard(timecard_id);
			  if(rowsDeleted >= 1){
			     return Response.ok("{\"success\": \"Timecard "+ timecard_id +" deleted.\"}").build();
			  }else{
			     return Response.ok("{\"error\": \"Timecard "+ timecard_id +" does not exist.\"}").build();
			  }
			}
		}
		catch(Exception e){
			  return Response.ok("{\"error\":\"" + e.getMessage() + "\"}").build();
		}
		finally{
			dl.close();
		}
	}
}
