<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

You are <b>subscribed</b> to an external concept dictionary.

<p>Name: ${dictionary.name}</p>
<p>URL: ${dictionary.subscriptionUrl}</p>
<p>Your dictionary is <b>locked</b> against local edits.</p>

<%-- save for version 7?
<form action="manage.form" method="POST">
<input type="submit" value="Cancel Subscription"
 onClick="if(confirm('Are you sure? This action is not reversible.'))
 return true;b
 else return false"/>
 </form>
--%>

Status<br/>
<c:choose>
<c:when test="${dictionary.imported}">
At version ${dictionary.version} (published ${dictionary.dateCreated})
</c:when>
<c:otherwise>
Not yet imported<br/>

New version available ${dictionary.version} (published ${dictionary.dateCreated})

<form action="updateToLatestVersion.form" method="POST">
<input type="submit" value="Update To Latest Version"/>
</form>
</c:otherwise>
</c:choose>



<%@ include file="/WEB-INF/template/footer.jsp"%>