@Grab('com.xlson.groovycsv:groovycsv:1.1')
import wslite.rest.*
import static com.xlson.groovycsv.CsvParser.parseCsv
import groovy.xml.*
import groovy.io.FileType
import groovy.json.JsonSlurper

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


def facit = [:]						   
int rowRest = 1

def jsonSlurper = new JsonSlurper()
def object = jsonSlurper.parseText(new File('fixtures.json').text)

//response.json
object.fixtures.each { row ->

	MatchResult matchResult = new MatchResult()
	matchResult.with{	
		dateToPlay = Date.parse( "yyyy-MM-dd'T'HH:mm:ss'Z'", row.date ).format( 'MM/dd HH:mm:ss' )
		playRound = rowRest
		homeTeam = country.get(row.homeTeamName) ?: row.homeTeamName
		awayTeam = country.get(row.awayTeamName) ?: row.awayTeamName
		homeScore = row.result.goalsHomeTeam 
		awayScore = row.result.goalsAwayTeam 
	}
	facit.put(rowRest, matchResult)
	rowRest++
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
				
					playRound = line."Grupp A - Tabell".toInteger()
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


def userPointPerRound=[:]
tipz.each{
	println it.userName
	it.results.each{ game->
	MatchResult test = facit.get(game.playRound)
	
	    Calculator.pointz(game, facit.get(game.playRound))
	}
}



class Calculator {
/*
Poängfördelning gruppspel
•	Rätt vinnare(1X2) 1p
•	Rätt resultat 3p

Poängfördelning slutspel 
•	Rätt lag till slutspel 2p
•	Åttondel - 2p/Lag, 1X2 2p, Resultat 6p
•	Kvartsfinal - 3p/Lag, 1X2 2p, Resultat 6p
•	Semi - 4p/Lag, 1X2 2p, Resultat 6p
•	Final - 5p/Lag, 1X2 2p, Resultat 6p
*/
	
   static pointz(MatchResult user, MatchResult facit){
		Integer pointz 
		if (user.matchResult() == facit.matchResult()) {
			pointz = roundPoint(user.playRound, '1X2')
			if(user.homeScore == facit.homeScore && user.awayScore == facit.awayScore){
				pointz += roundPoint(user.playRound, 'score')
			}
			println "Pointz: " + pointz + 'For player round: ' + user.playRound
		} else {
			println "Inte korrekt resultat"
		}
   }
   static roundPoint(Integer playRound, String type) {
	
	if (playRound < 36) {
		if (type == '1X2') {
			return 1
		} else {
			return 3
		}
	} else {
		if (type == '1X2') {
			return 2
		} else {
			return 6
		}
	}
   }

}

class MatchResult {
	String dateToPlay
	Integer playRound 
	String homeTeam
	String awayTeam
	String homeScore
	String awayScore
	
	void setHomeScore(def score){
		if (score == null) {
		  homeScore = '-1'
		 } else {
			homeScore = score
		 }
	}
	 
	void setAwayScore(def score){
		if (score == null) {
		  awayScore = '-1'
		 } else {
			awayScore = score
		 }
	}
	
	String toString(){
		return  playRound + ' - ' +homeTeam +  " - " + awayTeam + ' : ' + matchResult()
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