<div class="method">
	<h3>${name}</h3>
	<div class="methodMember">
		<div class="overrides">
		<#list overrides as over>
			<div class="override">
			${over}
			</div>
		</#list>
		</div>
		
		<div class="calls">
		<#list resolvedCalls as call>
			<div class="call">
			calls ${call}
			</div>
		</#list>
		<#list unknownCalls as call>
			<div class="call">
			calls ${call}
			</div>
		</#list>
		</div>
		
		<div class="purity">${purity}</div>
		
		<div class="effects">
		<#list effects as effect>
			<div class="effect">
			${effect}
			</div>
		</#list>
		</div>
		
		<pre><code>${forResults?html}</code></pre>
	</div>		
	<div class="asm">
<pre><code>${asm}</code></pre>
	</div>
	<div class="source">
<pre><code>${source?html}</code></pre></div>
</div>
