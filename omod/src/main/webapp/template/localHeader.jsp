<spring:htmlEscape defaultHtmlEscape="true" />
<ul id="menu">
	<li class="first"><a
		href="${pageContext.request.contextPath}/admin"><spring:message
				code="admin.title.short" /></a></li>

	<li
		<c:if test='<%= request.getRequestURI().contains("/subscribe") %>'>class="active"</c:if>>
		<a
		href="${pageContext.request.contextPath}/module/dictionarysubscribing/subscribe.form"><spring:message
				code="dictionarysubscribing.subscribe" /></a>
	</li>
	
	<!-- Add further links here -->
</ul>
<h2>
	<spring:message code="dictionarysubscribing.title" />
</h2>
