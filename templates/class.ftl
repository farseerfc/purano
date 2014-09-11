<h2>class ${name}</h2>
<div class="caches">
	fields: 
	<#list caches as cache>
		<p>${cache}</p>
	</#list> 
</div>
<div>
<div class="members">
	<#list methods as m>
		${m}
	</#list> 
</div>
</div>
