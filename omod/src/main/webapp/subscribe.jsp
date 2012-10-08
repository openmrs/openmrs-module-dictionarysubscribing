<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<p>You are <b>not subscribed</b> to an external concept dictionary.</p>

<c:if test="${conceptCount>0}">
<p>You already have ${conceptCount} concepts in your dictionary.</p>
<p style="background-color:yellow;">Trying to subscribe to an external dictionary that is not
consistent with these concepts may cause serious errors.
Proceed at your own risk!</p>
</c:if>

<form action="subscribed.form" method="POST">
 <fieldset>
  <legend>Subscribe to an external concept dictionary</legend>
  URL: <input type="text" name="url"><br>
 <input type="submit" value="Subscribe"
 onClick="if(confirm('You are about to subscribe to concept dictionary at '))
 return true;
 else return false"/>
 </fieldset>
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>