import java.nio.file.Paths

import com.github.jknack.handlebars.Handlebars
import com.github.jknack.handlebars.Template
import com.github.jknack.handlebars.io.FileTemplateLoader
import com.github.jknack.handlebars.io.TemplateLoader
import groovy.xml.XmlSlurper
import groovy.json.JsonSlurper

def mavenLicensesFile = properties["mavenLicensesFile"]
def editorNodeJsDepFile = properties["editorNodeJsDepFile"]
def editorBowerDepFile = properties["editorBowerDepFile"]
def pageNodeJsDepFile = properties["pageNodeJsDepFile"]
def templateDir = properties["templateDir"]
def outputFileName = properties["outputFileName"]
def bonitaMinorVersion = properties["bonitaMinorVersion"]

// == Maven

// parse and prepare license info
def licenseSummary = new XmlSlurper().parse(Paths.get(mavenLicensesFile));
def licenses = [:]
licenseSummary.dependencies.dependency.each {
    def licenceList = []
    it.licenses.license.each { licence ->
        licenceList.add([
                "name": licence.name,
                "url": licence.url
        ])
    }
    licenses["$it.groupId:$it.artifactId:$it.version"] = licenceList
}
// Gather maven deps
SortedSet mavenDeps = new TreeSet({a,b -> a.key <=> b.key})
project.artifacts.findAll { it.scope != 'test' }.each { entry ->
    def key = "$entry.groupId:$entry.artifactId:$entry.version"
    mavenDeps.add([
            "key": key,
            "groupId"   : entry.groupId,
            "artifactId": entry.artifactId,
            "version"   : entry.version,
            "licenses"  : licenses[key]
    ])
}

// == NodeJs

// Gather NodeJs Yarn deps
def jsonSlurper = new JsonSlurper()
Set pageDeps = extractYarnDeps(jsonSlurper, pageNodeJsDepFile)
Set nodeDeps = extractYarnDeps(jsonSlurper, editorNodeJsDepFile)

// Gather NodeJs Bower deps
nodeDeps.addAll(extractYarnDeps(jsonSlurper, editorBowerDepFile))

// Merge data and template and save adoc content to file
def data = [
        "project"  : [
                "name"   : "${project.artifactId}",
                "version": "${project.version}"
        ],
        "bonitaMinorVersion" : bonitaMinorVersion,
        "mavenDeps": mavenDeps,
        "nodeDeps": nodeDeps,
        "pageDeps": pageDeps
]
TemplateLoader loader = new FileTemplateLoader(templateDir);
Handlebars handlebars = new Handlebars(loader);
Template template = handlebars.compile("dependencies.adoc");
def adoc = template.apply(data)
new File(outputFileName).text = adoc

// =======================================================================================
// == methods
def extractYarnDeps(jsonSlurper, nodeJsDepFile) {
    SortedSet deps = new TreeSet({a,b -> a.key <=> b.key})
    def json = jsonSlurper.parse(new File(nodeJsDepFile))
    json.each { entry ->
        def dep = entry.key
        def name = dep.substring(0, dep.lastIndexOf('@'))
        def version = dep.substring(dep.lastIndexOf('@') + 1)
        def key = "$name:$version"
        deps.add([
                "key"    : key,
                "name"    : name,
                "version" : version,
                "licenses": isCollectionOrArray(entry.value.licenses)? entry.value.licenses : [entry.value.licenses]
        ])
    }
    return deps;
}
boolean isCollectionOrArray(object) {
    [Collection, Object[]].any { it.isAssignableFrom(object.getClass()) }
}
