import groovy.json.JsonSlurper
import groovy.transform.Field


println "Initializing Logstash-Elasticsearch Setup"

@Field
JsonSlurper jsonParser = new JsonSlurper()
@Field
String ELASTIC_URL = System.getenv("ELASTIC_URL")
@Field
def configuration = getConfiguration()

println "--------------------------------------------------------------"

updateElasticsearch(configuration)

println "--------------------------------------------------------------"

copyCompiledFilesToLogstash(configuration)

println "--------------------------------------------------------------"

// FUNCTIONS
def getConfiguration() {
    println "Reading index/pipeline configurations"
    def configurationJson = new File('/usr/share/logstash/alromos/configuration.json').getText('UTF-8')
    def configuration = jsonParser.parseText(configurationJson)

    println "Compiling Index Configurations"
    configuration.indices.each {
        it.templateFinalPath = "/usr/share/logstash/template/$it.templateFile"
        it.templateContent = new File("/usr/share/logstash/alromos/template/$it.templateFile").text
    }

    println "Compiling Pipeline Configurations"
    configuration.pipelines.each {
        it.pipelineContent = new File("/usr/share/logstash/alromos/pipeline/$it.pipelineFile").text
    }
    return configuration
}

def updateElasticsearch(configuration) {
    println "Creating indices and their templates in Elasticsearch"
    println "--------------------------------------------------------------"

    configuration.indices.each {
        if (!indexExists("$it.indexName")) {
            createIndexTemplate("$it.templateName", "$it.templateContent")
            createIndex("$it.indexName")
        }
    }
}

def copyCompiledFilesToLogstash(configuration) {
    println "Copying compiled configuration files to Logstash"
    println "--------------------------------------------------------------"

    def lsConfigurationFile = new File("/usr/share/logstash/config/pipelines.yml")
    lsConfigurationFile.text = ""
    configuration.pipelines.each {
        lsConfigurationFile.text += "\n- pipeline.id: $it.pipelineName"
        lsConfigurationFile.text += "\n  path.config: \"/usr/share/logstash/pipeline/$it.pipelineFile\""
        new File("/usr/share/logstash/pipeline/$it.pipelineFile").text = it.pipelineContent
    }
    configuration.indices.each {
        new File("$it.templateFinalPath").text = it.templateContent
    }

    def lsGlobalConfigFile = new File("/usr/share/logstash/config/logstash.yml")
    lsGlobalConfigFile.text =
            """
node.name: logstash-alromos
http.host: 0.0.0.0
http.port: 9600
monitoring.enabled: false
xpack.monitoring.enabled: false
config.support_escapes: true
pipeline.ecs_compatibility: disabled
allow_superuser: false
    """
}

boolean indexExists(String indexName) {
    def response = httpHead("$ELASTIC_URL/$indexName", Map.of())
    return response.responseCode == "200"
}

void createIndex(String indexName) {
    Map<String, String> headers = Map.of(
            "Content-Type", "application/json"
    )
    def response = httpPut("$ELASTIC_URL/$indexName", "", headers)
    httpPut(
            "$ELASTIC_URL/$indexName/_settings",
            "{\"index.number_of_replicas\" : \"0\"}",
            headers
    )
    println "- Created index $indexName. Status: $response.responseCode"
}

void createIndexTemplate(String templateName, String templateBody) {
    Map<String, String> headers = Map.of(
            "Content-Type", "application/json"
    )
    HttpResponse response = httpPost("$ELASTIC_URL/_index_template/$templateName", templateBody, headers)
    println "- Created template $templateName. Status: $response.responseCode"
}

static HttpResponse httpHead(String url, Map<String, String> headers) {
    def connection = new URL(url).openConnection()
    connection.setRequestMethod("HEAD")
    headers.entrySet().each { header ->
        connection.setRequestProperty(header.key, header.value)
    }
    HttpResponse response = new HttpResponse()
    try {
        response.responseCode = connection.getResponseCode()
        response.responseBody = connection.getInputStream().getText()
    } catch (FileNotFoundException e) {
        response.responseCode = "404"
    }
    return response
}

static HttpResponse httpPost(String url, String body, Map<String, String> headers) {
    def connection = new URL(url).openConnection()
    connection.setRequestMethod("POST")
    connection.setDoOutput(true)
    headers.entrySet().each { header ->
        connection.setRequestProperty(header.key, header.value)
    }
    connection.getOutputStream().write(body.getBytes("UTF-8"))
    HttpResponse response = new HttpResponse()
    response.responseCode = connection.getResponseCode()
    response.responseBody = connection.getInputStream().getText()
    return response
}

static HttpResponse httpPut(String url, String body, Map<String, String> headers) {
    def connection = new URL(url).openConnection()
    connection.setRequestMethod("PUT")
    connection.setDoOutput(true)
    headers.entrySet().each { header ->
        connection.setRequestProperty(header.key, header.value)
    }
    connection.getOutputStream().write(body.getBytes("UTF-8"))
    HttpResponse response = new HttpResponse()
    response.responseCode = connection.getResponseCode()
    response.responseBody = connection.getInputStream().getText()
    return response
}

class HttpResponse {
    String responseCode
    String responseBody
}
