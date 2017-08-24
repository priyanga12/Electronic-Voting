package com.sample.processserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.UserTransaction;


import org.apache.commons.logging.Log;
import javax.persistence.EntityManagerFactory;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.SystemEventListenerFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.impl.EnvironmentFactory;
import org.drools.io.ResourceFactory;
import org.drools.persistence.jpa.JPAKnowledgeService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.jbpm.process.audit.JPAProcessInstanceDbLog;
import org.jbpm.process.audit.JPAWorkingMemoryDbLogger;
import org.jbpm.process.workitem.wsht.SyncWSHumanTaskHandler;
import org.jbpm.task.AccessType;
import org.jbpm.task.TaskService;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.service.ContentData;
import org.jbpm.task.service.TaskServiceSession;
import org.jbpm.task.service.local.LocalTaskService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import bitronix.tm.TransactionManagerServices;


import antlr.collections.List;
import bitronix.tm.TransactionManagerServices;
import bitronix.tm.resource.jdbc.PoolingDataSource;

import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;

import com.sample.processserver.JbpmAPIUtil;

public class TaskProcessServlet extends HttpServlet {
//
//	 private static Log log = LogFactory.getLog(TaskProcessServlet.class);
//
//	 public void init() throws ServletException {
//
//	        super.init();
//
//	        try {
//
//
//			/*
//			 * Start local h2 datbase
//			 * This is not required if the application connects to a remote database
//			 */
//		
//			
//			try {
//				
//				DeleteDbFiles.execute("", "JPADroolsFlow", true);
//				 Server h2Server = Server.createTcpServer(new String[0]);
//				h2Server.start();
//			} catch (SQLException e) {
//				log.error(e.getMessage(), e.getCause());
//				throw new RuntimeException("can't start h2 server db",e);
//			}
//			
//
//	        	
//	        	UserTransaction ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
//	            ut.begin();
//
//
//	            StatefulKnowledgeSession ksession = JbpmAPIUtil.getSession();
//	            TaskService taskService = JbpmAPIUtil.getTaskService();
//	            
//                   System.setProperty("jbpm.usergroup.callback", "org.jbpm.task.service.DefaultUserGroupCallbackImpl");
//	        
//	        
//
//	          			ut.commit();
//				
//	            
//				
//			} catch (Throwable t) {
//				log.error(t.getMessage(), t.getCause());
//				throw new RuntimeException("error while creating session",t);
//			}
//			
//	 }
	 
	 protected void processRequest(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	        response.setContentType("text/html;charset=UTF-8");
	        PrintWriter out = response.getWriter();

	        try {
	            String action = request.getParameter("action");
	           
	            if (action.equals("CreateProcess")) {
	               
	            	UserTransaction ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
		            ut.begin();
		           
			        
			        /*
			         * Get the local task service
			         */
		            StatefulKnowledgeSession ksession = JbpmAPIUtil.getSession();
			        TaskService taskService = JbpmAPIUtil.getTaskService();
			        
			        
			        Map<String, Object> params = new HashMap<String, Object>();
	    			params.put("priority", "High");
	    			params.put("modelNumber", "123"); 
	    			params.put("quantity", "66"); 	    			
	            	
		          	ksession.startProcess("com.sample.bpmn.sampleHTformvariables",params);	
		          	
					ksession.fireAllRules();
					ut.commit();
		            
	                request.setAttribute("message", "Process Created!");
	                RequestDispatcher rD = request.getRequestDispatcher("adminuser.jsp");
	                rD.forward(request, response);
	            }
	            else if(action.equals("listtasks")) {
	            	String user = request.getParameter("user");
	            	
	            	//UserTransaction ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
	    			/*
	    			 * Get all the task assigned to 'user'
	    			 */
	                
	            	/*
	    			 * Retrive the tasks owned by a user
	    			 */
	            	TaskService taskService = JbpmAPIUtil.getTaskService();
	            	
	            	java.util.List<TaskSummary> tasks = taskService.getTasksAssignedAsPotentialOwner(user, "en-UK");
	    			
	    			
	    			
	    			System.out.println("\n***Task size::"+tasks.size()+"***\n");
	    			
	    			for (TaskSummary taskSummary : tasks) {   
	    			System.out.println(taskSummary.getId() + " :: "     + taskSummary.getActualOwner()); 
	    			}
	    			
	    			request.setAttribute("tasks",tasks);
	    			request.setAttribute("user",user);
	                RequestDispatcher rD = request.getRequestDispatcher("taskrequest.jsp");
	                rD.forward(request, response);
	    			
	            }
	            
	            else if(action.equals("Submit")) {
	            	
	            	UserTransaction ut = (UserTransaction) new InitialContext().lookup( "java:comp/UserTransaction" );
	            	 ut.begin();
	            	String user = request.getParameter("user");
	            	/*
	    			 * Get all the task assigned to 'user'
	    			 */
	            	 
	            	 
	            	long taskId = new Long(request.getParameter("taskId")).longValue();
	            	String taskStatus = request.getParameter("taskStatus");
	            	
	            	if (!taskStatus.equals("Completed")){
	            		
	            	TaskService taskService = JbpmAPIUtil.getTaskService();
	    			 //JbpmAPIUtil.completeTask(taskId, data, user);
	            		/*
	        			 * Start a task for krisv
	        			 */
	        			taskService.start(taskId, user);
	        			
	        			/*
	        			 * complete the task for krisv
	        			 */
	        			
	        			Map data = new HashMap();
		            	 data.put("priority",request.getParameter("priority"));
		            	 data.put("modelNumber",request.getParameter("modelNumber"));
		            	 data.put("quantity",request.getParameter("quantity"));
	        			
		            	 ContentData contentData = null;
		            	 if (data != null) {
		         			ByteArrayOutputStream bos = new ByteArrayOutputStream();
		         			ObjectOutputStream outs;
		         			try {
		         				outs = new ObjectOutputStream(bos);
		         				outs.writeObject(data);
		         				outs.close();
		         				contentData = new ContentData();
		         				contentData.setContent(bos.toByteArray());
		         				contentData.setAccessType(AccessType.Inline);
		         			} catch (IOException e) {
		         				e.printStackTrace();
		         			}
		         		}
		            	
		            	 /*
		            	 ContentData result = new ContentData();
		                 result.setAccessType(AccessType.Inline);
		                
		                 result.setType("java.util.HashMap");
		                 ByteArrayOutputStream bos = new ByteArrayOutputStream();
		                 ObjectOutputStream outr = new ObjectOutputStream(bos);
		                 outr.writeObject(data);
		                 outr.close();
		                 result.setContent(bos.toByteArray()); */
		                 
	        			taskService.complete(taskId, user, contentData);
	        			
	        			//logger.debug("completed the human task " );
	            	}
	            	
	    			ut.commit();
	    			
	    			//request.setAttribute("tasks",tasks);
	                RequestDispatcher rD = request.getRequestDispatcher("index.jsp");
	                rD.forward(request, response);		
	            }
	            else if(action.equals("taskinit")){
	            	
	            	
	            	request.setAttribute("taskId",request.getParameter("taskId"));
	            	request.setAttribute("taskStatus",request.getParameter("taskStatus"));
	            	request.setAttribute("user",request.getParameter("user"));
	            	request.setAttribute("processId",request.getParameter("processId"));
	            	
	            	
	                RequestDispatcher rD = request.getRequestDispatcher("initialrequest.jsp");
	                rD.forward(request, response);		
	            }
	        } catch(Exception e){
	            out.println("Error:"+ e.getMessage().toString());
	        }
	        finally {
	            out.close();
	        }
	        
	    } 
		 
	 
			 public void destroy() {
				    super.destroy();
				  }

				  protected void doGet(HttpServletRequest request, HttpServletResponse response)
				    throws ServletException, IOException
				  {
					  processRequest(request, response);
				    
				  }

				  protected void doPost(HttpServletRequest request, HttpServletResponse response)
				    throws ServletException, IOException
				  {
					  processRequest(request, response);
				     
				  }
}

