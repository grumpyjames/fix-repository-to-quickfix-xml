<b>Deploying Maven Artifact</b>
<h2>Introduction</h2>

Developers note on how to deploy artifact.
For details see <a href='https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide'>Sonatype OSS Maven Repository Usage Guide</a>.



<h2>Details</h2>

<h3>Prerequisites</h3>
gpg key created and known pass phrase. See <a href='https://docs.sonatype.org/display/Repository/How+To+Generate+PGP+Signatures+With+Maven'>How To Generate PGP Signatures With Maven</a>.
Currently tomac.org key exists.

<h3>Steps</h3>
<ol>
<li>add to .m2/settings.xml<br>
<pre><code>  &lt;servers&gt;<br>
    &lt;server&gt;<br>
      &lt;id&gt;sonatype-nexus-snapshots&lt;/id&gt;<br>
      &lt;username&gt;your-jira-id&lt;/username&gt;<br>
      &lt;password&gt;your-jira-pwd&lt;/password&gt;<br>
    &lt;/server&gt;<br>
    &lt;server&gt;<br>
      &lt;id&gt;sonatype-nexus-staging&lt;/id&gt;<br>
      &lt;username&gt;your-jira-id&lt;/username&gt;<br>
      &lt;password&gt;your-jira-pwd&lt;/password&gt;<br>
    &lt;/server&gt;<br>
  &lt;/servers&gt;<br>
</code></pre>
</li>
<li>enable maven-gpg-plugin in pom.xml (default uncommented)<br>
<pre><code>&lt;plugin&gt;<br>
        &lt;groupId&gt;org.apache.maven.plugins&lt;/groupId&gt;<br>
        &lt;artifactId&gt;maven-gpg-plugin&lt;/artifactId&gt;<br>
        &lt;executions&gt;<br>
                &lt;execution&gt;<br>
                        &lt;id&gt;sign-artifacts&lt;/id&gt;<br>
                        &lt;phase&gt;verify&lt;/phase&gt;<br>
                        &lt;goals&gt; <br>
                                &lt;goal&gt;sign&lt;/goal&gt;<br>
                        &lt;/goals&gt;<br>
                &lt;/execution&gt;<br>
        &lt;/executions&gt;<br>
&lt;/plugin&gt;<br>
</code></pre>
</li>
</ol>
<b>Deploy</b>
mvn clean deploy

Give the passphrase of the key.