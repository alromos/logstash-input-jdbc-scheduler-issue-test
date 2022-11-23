import groovy.json.JsonSlurper
import groovy.transform.Field


println "Initializing Logstash-Elasticsearch Setup"

@Field
JsonSlurper jsonParser = new JsonSlurper()
@Field
String ELASTIC_URL = System.getenv("ELASTIC_URL")
@Field
int PIPELINE_NUMBER = 60

println "--------------------------------------------------------------"

updateElasticsearch()

println "--------------------------------------------------------------"

copyCompiledFilesToLogstash()

println "--------------------------------------------------------------"

// FUNCTIONS
def updateElasticsearch() {
    println "Creating an index and its template in Elasticsearch"
    println "--------------------------------------------------------------"

    String indexName = "test-number"
    if (!indexExists(indexName)) {
        createIndexTemplate(indexName, new File("/usr/share/logstash/alromos/template/test-number.json").text)
        createIndex(indexName)
    }
}

def copyCompiledFilesToLogstash() {
    println "Copying compiled configuration files to Logstash"
    println "--------------------------------------------------------------"

    File lsConfigurationFile = new File("/usr/share/logstash/config/pipelines.yml")
    lsConfigurationFile.text = ""
    def pipelineTemplate = new File("/usr/share/logstash/alromos/pipeline/test-pipeline-0.conf").text
    for (int pipelineId = 0; pipelineId < PIPELINE_NUMBER; pipelineId++) {
        GString pipelineName = "test-pipeline-$pipelineId"
        new File("/usr/share/logstash/last_run/$pipelineName").text = "--- 2000-01-01 01:00:00.264304000 Z"
        GString pipelineConfigFilePath = "/usr/share/logstash/pipeline/${pipelineName}.conf"
        new File(pipelineConfigFilePath).text = pipelineTemplate.replace("[[START_SECOND]]", "$pipelineId")
                .replace("[[PIPELINE_NAME]]", pipelineName)
                .replace("[[NUMBER_ID]]", "$pipelineId")
        lsConfigurationFile.text += "\n- pipeline.id: $pipelineName"
        lsConfigurationFile.text += "\n  path.config: \"$pipelineConfigFilePath\""
    }

    // Index template
    new File("/usr/share/logstash/template/test-number.json").text =
            new File("/usr/share/logstash/alromos/template/test-number.json").text

    File lsGlobalConfigFile = new File("/usr/share/logstash/config/logstash.yml")
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
