/*********************************************************************************************
 * Copyright (c) 2014 Model-Based Systems Engineering Center, Georgia Institute of Technology.
 *                         http://www.mbse.gatech.edu/
 *                  http://www.mbsec.gatech.edu/research/oslc
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Eclipse Distribution License is available at
 *  http://www.eclipse.org/org/documents/edl-v10.php.
 *
 *  Contributors:
 *
 *	   Axel Reichwein, Koneksys (axel.reichwein@koneksys.com)		
 *******************************************************************************************/
package edu.gatech.mbsec.adapter.subversion;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;


import org.eclipse.lyo.oslc4j.core.annotation.OslcCreationFactory;
import org.eclipse.lyo.oslc4j.core.annotation.OslcQueryCapability;
import org.eclipse.lyo.oslc4j.core.annotation.OslcService;
import org.eclipse.lyo.oslc4j.core.model.OslcConstants;
import org.eclipse.lyo.oslc4j.core.model.OslcMediaType;





/**
 * This servlet contains the implementation of OSLC RESTful web services for File resources.
 * 
 * The servlet contains web services for: <ul margin-top: 0;>
 * <li> returning specific File resources in HTML and other formats </li>
 * <li> returning all File resources within a specific Subversion working directory
 *  in HTML and other formats </li>
 *  </ul>
 * 
 * @author Axel Reichwein (axel.reichwein@koneksys.com)
 */
@OslcService(SubversionConstants.SUBVERSION_FILE_DOMAIN)
@Path("subversionfiles/")
public class SubversionFileService {
	
	@Context
	private HttpServletRequest httpServletRequest;
	@Context
	private HttpServletResponse httpServletResponse;
	@Context
	private UriInfo uriInfo;
	
	@OslcQueryCapability(title = "File Query Capability", label = "File Catalog Query", resourceShape = OslcConstants.PATH_RESOURCE_SHAPES
			+ "/" + SubversionConstants.PATH_SUBVERSION_FILE, resourceTypes = { SubversionConstants.TYPE_SUBVERSION_FILE }, usages = { OslcConstants.OSLC_USAGE_DEFAULT })	
	@GET
	@Produces({ OslcMediaType.APPLICATION_RDF_XML,
			OslcMediaType.APPLICATION_XML, OslcMediaType.APPLICATION_JSON })
	public Collection<SubversionFile> getFiles()
			throws IOException, ServletException {				
		SubversionManager subversionManager = SubversionManager.getSubversionManager(uriInfo.getBaseUri().toString());		
		Collection<SubversionFile> subversionFiles = subversionManager.getSubversionFiles();
		return subversionFiles;
	}
	
	@GET	
	@Path("{uri}")
	@Produces({ OslcMediaType.APPLICATION_RDF_XML,
			OslcMediaType.APPLICATION_JSON })		
	public Response getFile(
//			@PathParam("uri") final String uri)
			@PathParam("uri") final String qualifiedName, @Context Request request)
	
			throws URISyntaxException {
		SubversionManager subversionManager = SubversionManager.getSubversionManager(uriInfo.getBaseUri().toString());
		SubversionFile subversionFile = subversionManager
				.getFileByURI(qualifiedName);
//		return subversionFile;
		EntityTag eTag = new EntityTag(String.valueOf(subversionFile.hashCode()));
		ResponseBuilder builder = request.evaluatePreconditions(eTag);
		
		//If rb is null then either it is first time request; or resource is modified
        //Get the updated representation and return with Etag attached to it
		if (builder == null) {
		    builder = Response.ok(subversionFile);
		}

		return builder.tag(eTag).build();
		
//		return Response.created(subversionFile.getAbout()).entity(subversionFile).build();
	}

	@GET
	@Path("{uri}")
	@Produces(MediaType.TEXT_HTML)
	public void getHtmlFile(
			@PathParam("uri") final String qualifiedName,
			@QueryParam("oslc.properties") final String propertiesString,
			@QueryParam("oslc.prefix") final String prefix)
			throws URISyntaxException, IOException {
		SubversionManager subversionManager = SubversionManager.getSubversionManager(uriInfo.getBaseUri().toString());
		SubversionFile subversionFile = subversionManager
				.getFileByURI(qualifiedName);
		String requestURL = httpServletRequest.getRequestURL().toString();
		if (subversionFile != null) {			
			httpServletRequest.setAttribute("resource", subversionFile);
			httpServletRequest.setAttribute("requestURL", requestURL);
			RequestDispatcher rd = httpServletRequest
					.getRequestDispatcher("/subversion/subversionfile_html.jsp");
			try {
				rd.forward(httpServletRequest, httpServletResponse);
			} catch (Exception e) {
				e.printStackTrace();
				throw new WebApplicationException(e);
			}
		}
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public void getHtmlFiles() {
		SubversionManager subversionManager = SubversionManager.getSubversionManager(uriInfo.getBaseUri().toString());
		Collection<SubversionFile> subversionFiles = subversionManager.getSubversionFiles();
		String requestURL = httpServletRequest.getRequestURL().toString();
		if (subversionFiles != null) {			
			httpServletRequest.setAttribute("elements", subversionFiles);
			httpServletRequest.setAttribute("requestURL", requestURL);			
			RequestDispatcher rd = httpServletRequest
					.getRequestDispatcher("/subversion/subversionfiles_html.jsp");
			try {
				rd.forward(httpServletRequest, httpServletResponse);
			} catch (Exception e) {
				e.printStackTrace();
				throw new WebApplicationException(e);
			}
		}
	}
	
	
}
