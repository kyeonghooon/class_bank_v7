<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- header.jsp -->
<%@ include file="/WEB-INF/view/layout/header.jsp"%>

<!-- start of context.jsp(xxx.jsp) -->
<div class="col-sm-8">
	<h2>계좌 상세 보기(인증)</h2>
	<h5>Bank App에 오신걸 환영합니다.</h5>

	<div class="bg-light p-md-5">
		<div class="user--box">
			${principal.username}님 계좌<br> 계좌번호 : ${account.number}<br> 잔액 : ${account.formatKoreanWon(account.balance)}
		</div>
		<br>
		<div>
			<a href="?type=all" class="btn btn-outline-primary">전체</a> <a href="?type=deposit" class="btn btn-outline-primary">입금</a> <a href="?type=withdrawal"
				class="btn btn-outline-primary">출금</a>
		</div>
		<br>
		<table class="table table-striped">
			<thead>
				<tr>
					<th>날짜</th>
					<th>보낸이</th>
					<th>받은이</th>
					<th>입출금 금액</th>
					<th>계좌잔액</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach items="${historyList}" var="historyAccount">
					<tr>
						<td>${historyAccount.timestampToString(historyAccount.createdAt)}</td>
						<td>${historyAccount.sender}</td>
						<td>${historyAccount.receiver}</td>
						<td>${historyAccount.formatKoreanWon(historyAccount.amount)}</td>
						<td>${historyAccount.formatKoreanWon(historyAccount.balance)}</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
		<br>
		<div class="d-flex justify-content-center">
			<ul class="pagination">
				<li class="page-item ${currentPage == 1 ? 'disabled' : ''}"><a class="page-link" href="?type=${type}&page=1">First</a></li>
				<li class="page-item ${currentPage == 1 ? 'disabled' : ''}"><a class="page-link" href="?type=${type}&page=${currentPage - 1}">Previous</a></li>
				<c:forEach begin="${startPage}" end="${endPage}" var="page">
					<li class="page-item ${page == currentPage ? 'active' : ''}"><a class="page-link" href="?type=${type}&page=${page}">${page}</a></li>
				</c:forEach>
				<li class="page-item ${currentPage == totalPage ? 'disabled' : ''}"><a class="page-link" href="?type=${type}&page=${currentPage + 1}">Next</a></li>
				<li class="page-item ${currentPage == totalPage ? 'disabled' : ''}"><a class="page-link" href="?type=${type}&page=${totalPage}">End</a></li>
			</ul>
		</div>
	</div>

</div>
<!-- end of col-sm-8 -->
</div>
</div>
<!-- end of context.jsp(xxx.jsp) -->

<!-- footer.jsp -->
<%@ include file="/WEB-INF/view/layout/footer.jsp"%>
