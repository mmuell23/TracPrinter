<?php
	class TicketCloser {
		private $db;
		
		private $prefix = "";
		private $mapping = array(
			"rmm" => "agilo",
			"branchenbuch" => "trac_branchenbuch",
			"agilo_zeitungsportal" => "agilo_zeitungsdienste"
		);
		
		public function __construct($project) {
			
			if(array_key_exists($project, $this->mapping)) {
				$project = $this->mapping[$project];
				$db = $project;
			} else {
				$db = $this->prefix.$project;
			}
			$server = mysql_connect("localhost", "root", "1UsiK!") or die("Keine Verbindung zur Datenbank!");
			$this->db = mysql_select_db($db, $server) or die("No Such DB");
		}
		
		public function closeTicket($ticket, $resolution, $status) {
			$time = time();
			$sql = "SELECT * FROM ticket WHERE id=$ticket";
			$qy = mysql_query($sql) or die($sql);
			
			if(!mysql_num_rows($qy)) {
				return false;
			} 
		
			$sql = "UPDATE ticket SET changetime=$time, status='$status', resolution='$resolution' WHERE id=$ticket";
			mysql_query($sql) or die($sql);
			
			$sql = "INSERT INTO ticket_change (ticket,time,author,field,newvalue,oldvalue)
			VALUES ($ticket, $time, 'admin', 'resolution', '$resolution','');";
			mysql_query($sql);
			
			$sql = "INSERT INTO ticket_change (ticket,time,author,field,newvalue,oldvalue)
			VALUES ($ticket, $time, 'admin', 'status', '$status','');";
			mysql_query($sql);
			
			//echo "Ticket $ticket auf Resolution $resolution und Status $status gesetzt.";
			
			return true;
		}
	}

	
	$project = $_GET["project"];
	$ticket = $_GET["ticket"];
	
	$tc = new TicketCloser($project);
	$in = $tc->closeTicket($ticket, "fixed", "closed");
	
	if($in)
?>
<html>
	<head>
		<meta name="viewport" content="user-scalable=no, width=device-width" />
		<style type="text/css">
			body { background: #dddddd; margin: 0; padding: 0;}
			#container { width: 100%;}
			* { font-family: arial, sans-serif; }
			h2 { margin: 10px; font-size: 24px; color: #000033; }
			#zitatdestages, p  { margin: 10px; }
		</style>
	</head>
	
	<body>
		<div id="container">
			<?php if($in): ?>
				<h2>Ticket closed</h2>
				<p>
					<b>Agilo Project:</b> <?php echo $_GET["project"]; ?>
				</p>
				<p>
					<b>Ticket:</b> <?php echo $_GET["ticket"]; ?>
				</p>
			<?php else: ?>
				<h2>Klappt irgendwie nicht...</h2>
				<pre>
				<?php
					print_r($_GET);
				?>
				</pre>
			<?php endif; ?>
			
 <?php
  echo file_get_contents(
    'http://www.zitate-online.de/zitatdestages.txt'
  );
 ?>
			
			
		</div>
	</body>
</html>
