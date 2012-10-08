<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

You are <b>subscribed</b> to an external concept dictionary.

<p>Name: {dictionary.name}</p>
<p>URL: {dictionary.subscriptionUrl}</p>
<p>Your dictionary is <b>locked</b> against local edits.</p>

<%-- save for version 7?
<form action="manage.form" method="POST">
<input type="submit" value="Cancel Subscription"
 onClick="if(confirm('Are you sure? This action is not reversible.'))
 return true;
 else return false"/>
 </form>
--%>
 
Status
At version {dictionary.version} (published {dictionary.dateCreated})
<c:if 

<%@ include file="/WEB-INF/template/footer.jsp"%>