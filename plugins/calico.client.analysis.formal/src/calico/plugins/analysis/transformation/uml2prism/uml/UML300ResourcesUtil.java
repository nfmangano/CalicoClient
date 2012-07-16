package calico.plugins.analysis.transformation.uml2prism.uml;

/*
 * Copyright (c) 2012 E.D.Willink and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   E.D.Willink - reverse migration based on UMLResourcesUtil and UMLUtil.
 *
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.ContentHandler;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.xmi.impl.RootXMLContentHandlerImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLContentHandlerImpl;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.UMLPlugin;
import org.eclipse.uml2.uml.resource.CMOF2UMLExtendedMetaData;
import org.eclipse.uml2.uml.resource.CMOF2UMLResource;
import org.eclipse.uml2.uml.resource.UML212UMLResource;
import org.eclipse.uml2.uml.resource.UML22UMLExtendedMetaData;
import org.eclipse.uml2.uml.resource.UML22UMLResource;
import org.eclipse.uml2.uml.resource.UMLResource;
import org.eclipse.uml2.uml.resource.XMI212UMLResource;
import org.eclipse.uml2.uml.resource.XMI2UMLExtendedMetaData;
import org.eclipse.uml2.uml.resource.XMI2UMLResource;
import org.eclipse.uml2.uml.util.UMLUtil;

public class UML300ResourcesUtil extends UMLUtil
{
	private static final ContentHandler XMI_CONTENT_HANDLER = new XMLContentHandlerImpl.XMI();

	private static final ContentHandler UML2_1_0_0_CONTENT_HANDLER = new RootXMLContentHandlerImpl(
		UML22UMLResource.UML2_CONTENT_TYPE_IDENTIFIER,
		new String[]{"uml2"}, //$NON-NLS-1$
		RootXMLContentHandlerImpl.XMI_KIND,
		UML22UMLResource.UML2_METAMODEL_NS_URI, null);

	private static final ContentHandler UML2_2_0_0_CONTENT_HANDLER = new RootXMLContentHandlerImpl(
		UMLResource.UML_2_0_0_CONTENT_TYPE_IDENTIFIER,
		new String[]{"uml"}, //$NON-NLS-1$
		RootXMLContentHandlerImpl.XMI_KIND,
		"http://www.eclipse.org/uml2/2.0.0/UML", null); //$NON-NLS-1$

	private static final ContentHandler UML2_2_1_0_CONTENT_HANDLER = new RootXMLContentHandlerImpl(
		UMLResource.UML_2_1_0_CONTENT_TYPE_IDENTIFIER,
		new String[]{"uml"}, //$NON-NLS-1$
		RootXMLContentHandlerImpl.XMI_KIND,
		UML212UMLResource.UML_METAMODEL_NS_URI, null);

	private static final ContentHandler UML2_3_0_0_CONTENT_HANDLER = new RootXMLContentHandlerImpl(
		UMLResource.UML_CONTENT_TYPE_IDENTIFIER, new String[]{"uml"}, //$NON-NLS-1$
		RootXMLContentHandlerImpl.XMI_KIND, UMLPackage.eNS_URI, null);

	private static final ContentHandler OMG_2_1_CONTENT_HANDLER = new RootXMLContentHandlerImpl(
		XMI2UMLResource.UML_2_1_CONTENT_TYPE_IDENTIFIER,
		new String[]{"xmi"}, //$NON-NLS-1$
		RootXMLContentHandlerImpl.XMI_KIND,
		XMI2UMLResource.UML_METAMODEL_2_1_NS_URI, null);

	private static final ContentHandler OMG_2_1_1_CONTENT_HANDLER = new RootXMLContentHandlerImpl(
		XMI2UMLResource.UML_2_1_1_CONTENT_TYPE_IDENTIFIER,
		new String[]{"xmi"}, //$NON-NLS-1$
		RootXMLContentHandlerImpl.XMI_KIND,
		XMI2UMLResource.UML_METAMODEL_2_1_1_NS_URI, null);

	private static final ContentHandler OMG_2_2_CONTENT_HANDLER = new RootXMLContentHandlerImpl(
		XMI2UMLResource.UML_2_2_CONTENT_TYPE_IDENTIFIER,
		new String[]{"xmi"}, //$NON-NLS-1$
		RootXMLContentHandlerImpl.XMI_KIND,
		XMI2UMLResource.UML_METAMODEL_2_2_NS_URI, null);

	private static final ContentHandler CMOF_2_0_CONTENT_HANDLER = new RootXMLContentHandlerImpl(
		CMOF2UMLResource.CMOF_CONTENT_TYPE_IDENTIFIER,
		new String[]{"cmof", "xmi"}, //$NON-NLS-1$ //$NON-NLS-2$
		RootXMLContentHandlerImpl.XMI_KIND,
		CMOF2UMLResource.CMOF_METAMODEL_NS_URI, null);

	/**
	 * Initializes the registries for the specified resource set (or the global
	 * registries if <code>null</code>) with the registrations needed to work
	 * with UML2 resources in stand-alone mode (i.e., without Eclipse).
	 * 
	 * @param resourceSet
	 *            The resource set whose registries to initialize, or
	 *            <code>null</code>.
	 * @return The resource set (or <code>null</code>).
	 * 
	 * @since 4.0
	 */
	public static ResourceSet init(ResourceSet resourceSet) {
		EPackage.Registry packageRegistry = resourceSet == null
			? EPackage.Registry.INSTANCE
			: resourceSet.getPackageRegistry();

		packageRegistry.put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);

		packageRegistry.put(
			"http://www.eclipse.org/uml2/2.0.0/UML", UMLPackage.eINSTANCE); //$NON-NLS-1$

		packageRegistry.put(UML212UMLResource.UML_METAMODEL_NS_URI,
			UMLPackage.eINSTANCE);

		packageRegistry.put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);

		Map<String, URI> ePackageNsURIToProfileLocationMap = UMLPlugin
			.getEPackageNsURIToProfileLocationMap();

		ePackageNsURIToProfileLocationMap.put(UMLResource.ECORE_PROFILE_NS_URI,
			URI.createURI("pathmap://UML_PROFILES/Ecore.profile.uml#_0")); //$NON-NLS-1$

		Map<URI, URI> uriMap = resourceSet == null
			? URIConverter.URI_MAP
			: resourceSet.getURIConverter().getURIMap();

		uriMap.put(URI.createURI(UMLResource.LIBRARIES_PATHMAP), URI
			.createPlatformPluginURI(
				"/org.eclipse.uml2.uml.resources/libraries/", true)); //$NON-NLS-1$
		uriMap.put(URI.createURI(UMLResource.METAMODELS_PATHMAP), URI
			.createPlatformPluginURI(
				"/org.eclipse.uml2.uml.resources/metamodels/", true)); //$NON-NLS-1$
		uriMap.put(URI.createURI(UMLResource.PROFILES_PATHMAP), URI
			.createPlatformPluginURI(
				"/org.eclipse.uml2.uml.resources/profiles/", true)); //$NON-NLS-1$

		List<ContentHandler> contentHandlers;

		if (resourceSet == null) {
			contentHandlers = ContentHandler.Registry.INSTANCE
				.get(ContentHandler.Registry.LOW_PRIORITY);

			if (contentHandlers == null
				|| !contentHandlers.contains(XMI_CONTENT_HANDLER)) {

				ContentHandler.Registry.INSTANCE.put(
					ContentHandler.Registry.LOW_PRIORITY, XMI_CONTENT_HANDLER);
			}

			contentHandlers = ContentHandler.Registry.INSTANCE
				.get(ContentHandler.Registry.NORMAL_PRIORITY);

			if (contentHandlers == null) {
				ContentHandler.Registry.INSTANCE.put(
					ContentHandler.Registry.NORMAL_PRIORITY,
					contentHandlers = new ArrayList<ContentHandler>());
			}
		} else {
			contentHandlers = resourceSet.getURIConverter()
				.getContentHandlers();
		}

		if (!contentHandlers.contains(UML2_1_0_0_CONTENT_HANDLER)) {
			contentHandlers.add(UML2_1_0_0_CONTENT_HANDLER);
		}

		if (!contentHandlers.contains(UML2_2_0_0_CONTENT_HANDLER)) {
			contentHandlers.add(UML2_2_0_0_CONTENT_HANDLER);
		}

		if (!contentHandlers.contains(UML2_2_1_0_CONTENT_HANDLER)) {
			contentHandlers.add(UML2_2_1_0_CONTENT_HANDLER);
		}

		if (!contentHandlers.contains(UML2_3_0_0_CONTENT_HANDLER)) {
			contentHandlers.add(UML2_3_0_0_CONTENT_HANDLER);
		}

		if (!contentHandlers.contains(OMG_2_1_CONTENT_HANDLER)) {
			contentHandlers.add(OMG_2_1_CONTENT_HANDLER);
		}

		if (!contentHandlers.contains(OMG_2_1_1_CONTENT_HANDLER)) {
			contentHandlers.add(OMG_2_1_1_CONTENT_HANDLER);
		}

		if (!contentHandlers.contains(OMG_2_2_CONTENT_HANDLER)) {
			contentHandlers.add(OMG_2_2_CONTENT_HANDLER);
		}

		if (!contentHandlers.contains(CMOF_2_0_CONTENT_HANDLER)) {
			contentHandlers.add(CMOF_2_0_CONTENT_HANDLER);
		}

		if (resourceSet != null
			&& !contentHandlers.contains(XMI_CONTENT_HANDLER)) {

			contentHandlers.add(XMI_CONTENT_HANDLER);
		}

		(resourceSet == null
			? Resource.Factory.Registry.INSTANCE
			: resourceSet.getResourceFactoryRegistry())
			.getContentTypeToFactoryMap().put(
				UMLResource.UML_CONTENT_TYPE_IDENTIFIER,
				UMLResource.Factory.INSTANCE);

		(resourceSet == null
				? Resource.Factory.Registry.INSTANCE
				: resourceSet.getResourceFactoryRegistry())
				.getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION,
					UMLResource.Factory.INSTANCE);
			
		Map<String, Object> contentTypeToFactoryMap = (resourceSet == null
			? Resource.Factory.Registry.INSTANCE
			: resourceSet.getResourceFactoryRegistry())
			.getContentTypeToFactoryMap();

		contentTypeToFactoryMap.put(
			UML212UMLResource.UML_2_1_0_CONTENT_TYPE_IDENTIFIER,
			UML212UMLResource.Factory.INSTANCE);
		contentTypeToFactoryMap.put(
			UML22UMLResource.UML2_CONTENT_TYPE_IDENTIFIER,
			UML22UMLResource.Factory.INSTANCE);
		contentTypeToFactoryMap.put(
			XMI2UMLResource.UML_CONTENT_TYPE_IDENTIFIER,
			XMI2UMLResource.Factory.INSTANCE);
		contentTypeToFactoryMap.put(
			XMI212UMLResource.UML_2_1_1_CONTENT_TYPE_IDENTIFIER,
			XMI212UMLResource.Factory.INSTANCE);
		contentTypeToFactoryMap.put(
			XMI212UMLResource.UML_2_1_CONTENT_TYPE_IDENTIFIER,
			XMI212UMLResource.Factory.INSTANCE);
		contentTypeToFactoryMap.put(
			CMOF2UMLResource.CMOF_CONTENT_TYPE_IDENTIFIER,
			CMOF2UMLResource.Factory.INSTANCE);

//		uriMap.putAll(UML302UMLExtendedMetaData.getURIMap());
//		uriMap.putAll(UML212UMLExtendedMetaData.getURIMap());
		uriMap.putAll(UML22UMLExtendedMetaData.getURIMap());
		uriMap.putAll(XMI2UMLExtendedMetaData.getURIMap());
		uriMap.putAll(CMOF2UMLExtendedMetaData.getURIMap());
			
		return resourceSet;
	}

}