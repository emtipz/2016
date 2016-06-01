@Grab('com.xlson.groovycsv:groovycsv:1.1')
import wslite.rest.*
import static com.xlson.groovycsv.CsvParser.parseCsv
import groovy.xml.*
import groovy.io.FileType

def country = [France:'Frankrike', 'Northern Ireland': 'Nordirland']
def client = new RESTClient("http://api.football-data.org/v1/")
def response = client.get( path:'/soccerseasons/424/fixtures',
                           accept: ContentType.JSON,
                           headers:["X-Auth-Token":"58e96927bea04cb2a36c3930ed1a2c7d"],
                           connectTimeout: 5000,
                           readTimeout: 10000,
                           followRedirects: false,
                           useCaches: false,
                           sslTrustAllCerts: true )


def facit = []						   

response.json.fixtures.each { row ->
	
	MatchResult matchResult = new MatchResult()
	matchResult.with{	
		dateToPlay = Date.parse( "yyyy-MM-dd'T'HH:mm:ss'Z'", row.date ).format( 'MM/dd HH:mm:ss' )
		playRound = '1'
		homeTeam = country.get(row.homeTeamName) ?: row.homeTeamName
		awayTeam = country.get(row.awayTeamName) ?: row.awayTeamName
		homeScore = row.result.goalsHomeTeam ?: '-1'
		awayScore = row.result.goalsAwayTeam ?: '-1'
	}
	facit.add(matchResult)
	
} 
Tipz tipz = new Tipz()
new File("users").eachFile() { file->  
	String fileName =  file.getName().split("\\.")[0]
	
	tipz.userName = fileName
	def data = parseCsv(file.getText('ISO-8859-1'))
	int row = 1
		data.each { line ->
			if (row > 8 && row < 15) {
				MatchResult match = new MatchResult()
				match.with {
				
					playRound = line."Grupp A - Tabell"
					homeTeam = line.HemmaLag
					awayTeam = line.BortaLag
					homeScore = line.HemmaScore
					awayScore = line.BortaScore
				}
				tipz.results.add(match)
					
			}
			
			row++
		}	
}



facit.each {
	println it 
}
tipz.each{
	println it.userName
	it.results.each{ game->
		println game 
	}
}





class MatchResult {
	String dateToPlay
	String playRound 
	String homeTeam
	String awayTeam
	String homeScore
	String awayScore
	
	String toString(){
		return  dateToPlay + ' - ' +homeTeam +  " - " + awayTeam + ' : ' + matchResult()
	}
	
	String matchResult() {
		if (homeScore.toInteger() < 0) {
			return 'N/P'
		} else {
		if (homeScore.toInteger() > awayScore.toInteger()) {
			return '1'
		} else if (homeScore.toInteger() == awayScore.toInteger()) {
			return 'X'
		} else {
			return '2'
		}
		}
	}

} 

class Tipz {
	String userName
	List<MatchResult> results = []
}