FIX Repository to <a href='http://www.quickfixengine.org/xml.html'>QuickFix XML</a> is a FIX Repository XML (from <a href='http://fixprotocol.org'>fixprotocol.org</a>) to QuickFix formatted XML converter hosted at <a href='http://code.google.com/p/fix-repository-to-quickfix-xml/'><a href='http://code.google.com/p/fix-repository-to-quickfix-xml/'>http://code.google.com/p/fix-repository-to-quickfix-xml/</a></a>.
QuickFix XML is used for creating FIX protocol message container code generation by <a href='http://code.google.com/p/to-fix/'>to-fix</a>.
<h2>Summary</h2>
FIX Repository to <a href='http://code.google.com/p/fix-repository-to-quickfix-xml/wiki/QuickFix'>QuickFix</a> XML is a FIX Repository XML (from <a>fixprotocol.org</a>) to <a href='http://code.google.com/p/fix-repository-to-quickfix-xml/wiki/QuickFix'>QuickFix</a> formatted XML converter. <a>QuickFixXML</a> is used for creating FIX protocol message container code generation.
<h3>Usage</h3>
Prerequisites: Java 1.6 or later installed
<ol>
<blockquote><li>Download the latest fix-repository-to-quickfix-xml.jar</li>
<li>Download and unpack the FIX Repository XML. Get the FIX repository from your exchange or FIX <a href='http://www.fixprotocol.org/repository'><a href='http://www.fixprotocol.org/repository'>http://www.fixprotocol.org/repository</a></a> (the <i>Repository 2008 Edition</i>). Unpack the zip file, and you will have a directory containg atleast the following files:<br>
<pre><code>Components.xml<br>
Enums.xml<br>
Fields.xml<br>
MsgContents.xml<br>
MsgType.xml</code></pre>
</li>
<li>Run the fix-repository-to-quickfix-xml converter;<br>
<pre><code>java -DisStrictQuickFix=true -DfixVersion=&lt;fix_version&gt;<br>
-jar fix-repository-to-quickfix-xml-&lt;version&gt;.jar<br>
&lt;directory_with_fix_repository&gt; &lt;name_of_new_quickfix_xml_file&gt;</code></pre>
</li>
</ol>
<strong>Example:</strong> assuming unpacking the repository in C:\temp\repository and the downloaded jar in C:\temp.<br>
<pre><code>java -DisStrictQuickFix=true -DfixVersion=FIX.5.0SP2<br>
-jar "C:\temp\fix-repository-to-quickfix-xml-1.0.jar"<br>
"C:\temp\repository" "C:\temp\FIX50.xml"</code></pre>
To generate FIXT.1.1 for FIX.5.0<br>
<pre><code>java -DisStrictQuickFix=true -DfixVersion=FIXT.1.1<br>
-jar "C:\temp\fix-repository-to-quickfix-xml-1.0.jar"<br>
"C:\temp\repository" "C:\temp\FIXT11.xml"</code></pre>
<span>XML</span></blockquote>

Sample of QuickFixXML based on FIX Repository XML
<ul>
<blockquote><li><a href='http://tomac.org/download/QuickFixNordicINETFIX42.xml'>Nasdaq OMX Nordic Equities</a></li>
<li><a href='http://tomac.org/download/QuickFixNordicGeniumINETFIX44.xml'>Nasdaq OMX Nordic Derivatives and Fixed Income</a></li>
</ul>
<h3>Download</h3>
Download the latest release of the FIX Repository to QuickFixXML converter from <a href='http://code.google.com/p/fix-repository-to-quickfix-xml/downloads/list'><a href='http://code.google.com/p/fix-repository-to-quickfix-xml/downloads/list'>http://code.google.com/p/fix-repository-to-quickfix-xml/downloads/list</a></a>,  or if you are using maven as build add the dependency<br>
<pre><code>&lt;dependencies&gt;<br>
  &lt;dependency&gt;<br>
    &lt;groupId&gt;org.tomac&lt;/groupId&gt;<br>
    &lt;artifactId&gt;fix-repository-to-quickfix-xml&lt;/artifactId&gt;<br>
    &lt;version&gt;1.0-SNAPSHOT&lt;/version&gt;<br>
  &lt;/dependency&gt;<br>
&lt;/dependencies&gt;<br>
</code></pre>