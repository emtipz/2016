@Grab('com.xlson.groovycsv:groovycsv:1.1')
import wslite.rest.*
import static com.xlson.groovycsv.CsvParser.parseCsv
import groovy.xml.*
import groovy.io.FileType
import groovy.json.JsonSlurper

//Translation list 
def country = [France:'Frankrike', 'Northern Ireland': 'Nordirland', Sweden: 'Zverige', Belgium: 'Belgien', Italy: 'Italien', 'Republic of Ireland': 'Irland', Hungary: 'Ungern', Iceland: 'Island', Austria: 'Österrike', Croatia: 'Kroatien', Spain: 'Spanien', Turkey: 'Turkiet', 'Czech Republic': 'Tjeckien', Germany: 'Tyskland', Poland: 'Polen', Ukraine: 'Ukraina', Russia: 'Ryssland', Slovakia: 'Slovakien', Switzerland: 'Schweiz', Albania: 'Albanien', Romania: 'Rumänien']
//Read up the teams for the playoffs to count the points 
def config = new ConfigSlurper().parse(new File('playoff.groovy').toURL())


def client = new RESTClient("http://api.football-data.org/v1/")
/*
def response = client.get( path:'/soccerseasons/424/fixtures',
                           accept: ContentType.JSON,
                           headers:["X-Auth-Token":"58e96927bea04cb2a36c3930ed1a2c7d"],
                           connectTimeout: 5000,
                           readTimeout: 10000,
                           followRedirects: false,
                           useCaches: false,
                           sslTrustAllCerts: true )

*/
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
def allTipz=[]
new File("users").eachFile() { file->  
	String fileName =  file.getName().split("\\.")[0]
	Tipz tipz = new Tipz()
	tipz.userName = fileName
	def data = parseCsv(file.getText('ISO-8859-1'))
	int row = 1
		data.each { line ->
			if (row > 8 && row < 15) {
				tipz.results.add(addMatchResults(line."Grupp A - Tabell",line.HemmaLag,line.BortaLag,line.HemmaScore,line.BortaScore))
				tipz.results.add(addMatchResults(line."Grupp B - Tabell",line.HemmaLag2,line.BortaLag2,line.HemmaScore2,line.BortaScore2))
			}
			if (row>25 && row < 32) {
				tipz.results.add(addMatchResults(line."Grupp A - Tabell",line.HemmaLag,line.BortaLag,line.HemmaScore,line.BortaScore))
				tipz.results.add(addMatchResults(line."Grupp B - Tabell",line.HemmaLag2,line.BortaLag2,line.HemmaScore2,line.BortaScore2))
			}
			if (row>42 && row < 49) {
				tipz.results.add(addMatchResults(line."Grupp A - Tabell",line.HemmaLag,line.BortaLag,line.HemmaScore,line.BortaScore))
				tipz.results.add(addMatchResults(line."Grupp B - Tabell",line.HemmaLag2,line.BortaLag2,line.HemmaScore2,line.BortaScore2))
			}
			if (row>52 && row < 61) {
				tipz.results.add(addMatchResults(line."Grupp B - Tabell",line.HemmaLag2,line.BortaLag2,line.HemmaScore2,line.BortaScore2))
			}
			if (row>64 && row<67) {
				tipz.results.add(addMatchResults(line."Grupp B - Tabell",line.HemmaLag2,line.BortaLag2,line.HemmaScore2,line.BortaScore2))
			}
			if (row>71 && row<73) {
				tipz.results.add(addMatchResults(line."Grupp B - Tabell",line.HemmaLag2,line.BortaLag2,line.HemmaScore2,line.BortaScore2))
			}
			
			row++
		}
	allTipz.add(tipz)
}

def addMatchResults(def thisPlayRound, def thisHomeTeam, def thisAwayTeam, def thisHomeScore, def thisAwayScore){		
	MatchResult match = new MatchResult()
		match.with {
			playRound = thisPlayRound.toInteger()
			homeTeam = thisHomeTeam.trim()
			awayTeam = thisAwayTeam.trim()
			homeScore = thisHomeScore
			awayScore = thisAwayScore
		}
		
		return match
}


def perMatchResult=[:]
allTipz.each{ tipz->

	tipz.each {
		println it.userName
		it.results.each{ game->
			//println "INFO: $game.homeTeam - $game.awayTeam: $game.homeScore - $game.awayScore"
			// from config we set what games that has been played. 
			if (game.playRound < config.playedRounds) {
				perMatchResult.put(name: it.userName, round:game.playRound , hometeam: game.homeTeam, awayteam: game.awayTeam, Calculator.pointz(game, facit.get(game.playRound)))
				
			}
		}
	}
}


def writer = new FileWriter('EM2016.html')
def src = new groovy.xml.MarkupBuilder(writer)
src.html {
  head {
    title 'Forza South EM Tipz'
  }
  meta (charset:"UTF-8")
  body {
    
  }
  script(src: 'sorttable.js'){mkp.yield("")}

  style (""" 
/* 
Enfo colors
blue: #225E9B;
orange: #FBFBFB; 
gray: #FFFFFF;

*/
body {
  background-image:url('img/em.png');
}
body, table, form, input, td, th, p, textarea, select{
  font-family: "Trebuchet MS", Trebuchet, Candara, Arial, Helvetica, Sans-Serif;
  font-size: 12px;

}  
div.row {
    margin: 0;
    overflow: hidden;
    padding: 0;
    width: 98.5%;
}
div.cols2 {
    float: left;
    margin: 0 3px 0 0;
    padding: 0;
    width: 49%;
}    
                             

th {
 background-color: #EEEEEE;
 border: 1px solid #CCCCCC;
 color: #555555;
 padding: 8px;
 text-align: center;
}
 th test{
background-color: #FFFFFFF;
}
table.middle {
 margin-left:auto; 
    margin-right:auto;
}
tbody.tuff th {
padding: 0px;
}
table {
 border-collapse: collapse;
 border-spacing: 0;
}
table.left{
float: left;
   margin-left:auto; 
    margin-right:auto;
}
table.right{
float: right;
   margin-left:auto; 
    margin-right:auto;
}
td {
 border: 1px solid #CCCCCC;
 padding: 5px 10px;
 vertical-align: top;
  background-color: #FFF;
}

caption {
 text-align: center;
 height: 80px; 
 color: #225E9B; 
 font-size: 20px; 
 background-color: #EFEFEF;
 border: 1px solid #CCCCCC;
 border-bottom: none;
 font-weight: bold;
 padding: 10px;}""")
 
    table (class: 'right') {
	tr {
		th ('Match')
		th ('Resultat')
	}
	/*
		String dateToPlay
		Integer playRound 
		String homeTeam
		String awayTeam
		String homeScore
		String awayScore
	*/
	facit.each{ matchScore->
		tr {
			td ("${matchScore.key}. ${matchScore.value.homeTeam} - ${matchScore.value.awayTeam}")
			if (matchScore.value.homeScore.toInteger() < 0) {
				td ("N/P")
			} else {
				td ("${matchScore.value.homeScore} - ${matchScore.value.awayScore}")
			}
		
		}
	}
  
  }
  
  table (class:'middle'){ 
	//Calculator.pointz(game, facit.get(game.playRound))
	caption 'ENFO - Forza South EM Tipz'
	thead {	tr { th() 
		def allName = perMatchResult.collect { it.key.name }.unique()
					
					allName.each {
					
						td(it)
					}
				
				}
			}
	tbody {tr{th(scope:'rowgroup', colspan:'100%' , class: 'tuff'){mkp.yield("Poäng per match")}}
			
			def thisgames = ''
			perMatchResult.collect { it.key.round }.unique().sort().each{ match ->
				
					perMatchResult.find{it.key.round == match}.each { 
						thisgames = it.key.round + '. ' + it.key.hometeam + ' - ' + it.key.awayteam
						tr{ th(thisgames) {
							perMatchResult.findAll{it.key.round == match}.each {	
									
									
									td (it.value)
							}
								}
							}						
						}
					 
				
				
					}
				
				}	
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
		Integer pointz = 0
		if (user.matchResult() == facit.matchResult()) {
			pointz = roundPoint(user.playRound, '1X2')
			if(user.homeScore == facit.homeScore && user.awayScore == facit.awayScore){
				pointz += roundPoint(user.playRound, 'score')
			}
			
			
		}
			
		return pointz
   }
   static playoffPoints(List userPlayOffTeams, List facitPlayOffTeams, String playoff) {
		def commons = userPlayOffTeams.intersect(facitPlayOffTeams)
		if (playoff=='eighth') {
			return commons.size().toInteger() * 2
		}else if (playoff=='qurter') {
			return commons.size().toInteger() * 3
		}else if (playoff=='semi') {
			return commons.size().toInteger() * 4
		} else {
			return commons.size().toInteger() * 5
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