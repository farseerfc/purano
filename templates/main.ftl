<html>
<head>
<style>
.clazz {
	margin-left: 20px;
}
.members {
	margin-left: 20px;
}
.method{
	margin-left: 20px;
	clear: both;
}
.purity{
	margin-left: 20px;
}
.effects{
	margin-left: 20px;
}
.calls{
	margin-left: 20px;
}
.clazz{
	margin-left: 20px;
	clear: both;
}
.source{
	float: left;
	width: 34%;
}
.methodMember{
	float: left;
	width: 33%;
}
.asm{
	float: left;
	width: 33%;
}
</style>
</head>
<body>
<div>
<#list package as p>
package ${p} ;
</#list>
</div>
<div>
<#list imports as import>
<p>import ${import} ;</p>
</#list>
</div>
<#list classes as class>
	<div class="clazz">
		${class}
	</div>
</#list>
<#list stat as s>
	<div class="clazz">
		${s}
	</div>
</#list>
</body>
</html>