<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<p>
	You are <b>subscribed</b> to an external concept dictionary.
</p>

<table>
	<tr>
		<td>&nbsp;Name: ${dictionary.name}<br /> &nbsp;URL: <a
			href="${dictionary.subscriptionUrl}">${dictionary.subscriptionUrl}</a>
		</td>
		<td>
			<form action="unsubscribe.form" method="POST">
				<input type="hidden" name="url"
					value="${dictionary.subscriptionUrl}" /> <input type="submit"
					value="Cancel Subscription"
					onClick="if(confirm('Are you sure? This action is not reversible.'))
 return true;
 else return false" />
			</form>
		</td>
	</tr>
</table>

<p>
	Your dictionary is <b><c:if test="${!locked}">not</c:if> locked</b>
	against local edits.
</p>

<i>Status</i>
<br />
<c:choose>
	<c:when test="${!empty dictionary.version}">
At <b>version ${dictionary.version}</b>
	</c:when>
	<c:otherwise>
		<b>Not yet imported</b>
		
	</c:otherwise>
</c:choose>

<br />

<c:choose>
	<c:when
		test="${!dictionary.subscriptionStatus.upToDate}">
		<b>New version available:</b>
version ${dictionary.remoteVersion} (published
${dictionary.dateCreated})
<table>
			<tr>
				<td>
					<form action="updateToLatestVersion.form" method="POST">
						<input type="submit" value="Update To Latest Version" />
					</form>
				</td>
				<td>
					<p style="background-color: yellow;">Avoid updating during data
						entry hours and test all your forms after updating.</p>
				</td>
			</tr>
		</table>
	</c:when>
	<c:otherwise>
		<b>Up to date</b>
		<form action="checkForUpdates.form" method="POST">
			<input type="submit" value="Check For Updates" />
		</form>
	</c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/template/footer.jsp"%>