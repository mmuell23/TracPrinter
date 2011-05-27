<?php

	class QrReceiver {
		/* configuration of six service */
		const HTTP_BASIC = "admin:admin";
		
		private $ch;
		private $url;
		private $offset;
		private $json_return;
		private $cookie_jar;
		private $form_token;
		
		private $timezone = -2;
		
		/**
		* Create instance by URL and optional offset value for time adjustment
		**/
		public function __construct($url, $offset=30) {
			$this->url = $url;
			$this->offset = $offset;
			$this->cookie_jar = tempnam('/tmp','cookie');
		}
		
		
		public function resolve($id) {
			echo "RESOLVE";
			$this->getFormToken();
			$this->init();
			curl_setopt($this->ch, CURLOPT_POST, true); 
			curl_setopt($this->ch, CURLOPT_CUSTOMREQUEST, 'POST'); 
			curl_setopt($this->ch, CURLOPT_HEADER, false );
			//curl_setopt($this->ch, CURLOPT_POSTFIELDS, $this->buildPostParameters($data_array)); 
			echo $this->form_token;
			$date = date("Y-m-d H:i:s+00:00", (time() - 2 * 60 * 60));
			$date = "2011-05-12 15:33:40+00:00";
			$params = "__FORM_TOKEN=".$this->form_token."&action=leave&comment=CurlComment&ts=".urlencode($date)."&replyto=&cnum=2&submit=Submit+changes";
			curl_setopt($this->ch, CURLOPT_POSTFIELDS, $params);
			//curl_setopt($this->ch, CURLOPT_HTTPHEADER, array("Content-Type: text/xml") );		
			curl_setopt($this->ch, CURLOPT_RETURNTRANSFER, true);
			curl_setopt($this->ch, CURLOPT_FOLLOWLOCATION, true);
			curl_setopt($this->ch, CURLOPT_RETURNTRANSFER, 1);
			curl_setopt($this->ch, CURLOPT_URL, $this->url."/ticket/$id");
			return $this->exec();			
		}
		
		public function comment($id) {
			$this->init();
			curl_setopt($this->ch, CURLOPT_POST, true); 
			curl_setopt($this->ch, CURLOPT_CUSTOMREQUEST, 'POST'); 
			curl_setopt($this->ch, CURLOPT_HEADER, false );
			//curl_setopt($this->ch, CURLOPT_POSTFIELDS, $this->buildPostParameters($data_array)); 
			curl_setopt($this->ch, CURLOPT_POSTFIELDS, "action=leave&comment=test&ts=2011-02-04+11%3A58%3A57%2B00%3A00&replyto=&cnum=2&submit=Submit+changes");
			//curl_setopt($this->ch, CURLOPT_HTTPHEADER, array("Content-Type: text/xml") );		
			curl_setopt($this->ch, CURLOPT_RETURNTRANSFER, true);
			curl_setopt($this->ch, CURLOPT_FOLLOWLOCATION, true);
			curl_setopt($this->ch, CURLOPT_RETURNTRANSFER, 1);
			curl_setopt($this->ch, CURLOPT_URL, $this->url."/ticket/$id");
			return $this->exec();	
		}
		
		public function login() {
			echo "LOGIN\n";
			$this->init();
			curl_setopt($this->ch, CURLOPT_HEADER, 0);
			curl_setopt($this->ch, CURLOPT_RETURNTRANSFER, true);
			curl_setopt($this->ch, CURLOPT_TIMEOUT, 10);		
			curl_setopt($this->ch, CURLOPT_URL, $this->url."/login");	
			return $this->exec();	
		}		
		
		public function loadTicket($id) {
			echo "LOAD TICKET\n";
			$this->init();
			curl_setopt($this->ch, CURLOPT_HEADER, 0);
			curl_setopt($this->ch, CURLOPT_RETURNTRANSFER, true);
			curl_setopt($this->ch, CURLOPT_TIMEOUT, 10);		
			curl_setopt($this->ch, CURLOPT_RETURNTRANSFER, 1);
			
			$params = "";
			foreach($data as $key=>$value) {
				$params .= "$key=".urlencode($value)."&";
			}
			curl_setopt($this->ch, CURLOPT_URL, $this->url."/ticket/$id");	
			return $this->exec();				
		}
		
		/**
		* POST Data to Six
		**/		
		public function post($data_array) {
			$this->init();
			curl_setopt($this->ch, CURLOPT_POST, true); 
			curl_setopt($this->ch, CURLOPT_CUSTOMREQUEST, 'POST'); 
			curl_setopt($this->ch, CURLOPT_HEADER, false );
			curl_setopt($this->ch, CURLOPT_POSTFIELDS, $this->buildPostParameters($data_array)); 
			curl_setopt($this->ch, CURLOPT_RETURNTRANSFER, true);
			curl_setopt($this->ch, CURLOPT_FOLLOWLOCATION, true);
			curl_setopt($this->ch, CURLOPT_RETURNTRANSFER, 1);
			curl_setopt($this->ch, CURLOPT_URL, $this->url);
			return $this->exec();
		}
		
		/**
		* GET Data from Six
		**/
		public function get($data) {
			$this->init();
			curl_setopt($this->ch, CURLOPT_HEADER, 0);
			curl_setopt($this->ch, CURLOPT_RETURNTRANSFER, true);
			curl_setopt($this->ch, CURLOPT_TIMEOUT, 10);		
			curl_setopt($this->ch, CURLOPT_RETURNTRANSFER, 1);
			
			$params = "";
			foreach($data as $key=>$value) {
				$params .= "$key=".urlencode($value)."&";
			}
			curl_setopt($this->ch, CURLOPT_URL, $this->url."?$params".$this->build_hash_string());	
			return $this->exec();	
		}		
		
		private function getFormToken() {
			echo "GET FORM TOKEN\n";
			$this->form_token = "";
			$f = file($this->cookie_jar);
			foreach($f as $line) {
				echo "$line\n";
				$p = strpos($line, "trac_form_token");
				if($p > 0) {
					$this->form_token = trim(substr($line, $p + 15));
				}
			}
		}
		
		private function buildPostParameters($data_array) {
			$string = "";
			foreach($data_array as $key => $value) {
				$string .= $key."=".$value."&";
			}
			return $string;
		}
		
		private function init() {
			$this->ch = curl_init();
			if(QrReceiver::HTTP_BASIC) {
				curl_setopt($this->ch, CURLOPT_HTTPAUTH, CURLAUTH_ANY); 
				curl_setopt($this->ch, CURLOPT_USERPWD, QrReceiver::HTTP_BASIC);	
			}		
			curl_setopt($this->ch, CURLOPT_COOKIEJAR, $this->cookie_jar);
			curl_setopt($this->ch, CURLOPT_COOKIEFILE, $this->cookie_jar);	
		}
		
		private function exec() {
			$output = curl_exec($this->ch);
			$status = curl_getinfo($this->ch, CURLINFO_HTTP_CODE);
		
			$retries = 5;
			$done = false;
			while($retries > 0 && !$done) {
				if($status == "200") {
					$done = true;
				} else {
					$output = curl_exec($this->ch);
					$status = curl_getinfo($this->ch, CURLINFO_HTTP_CODE);	
				}
				$retries = $retries - 1;
			}		
			
			echo "<pre>";
			print_r(curl_errno($this->ch));
			print_r(curl_getinfo($this->ch));
			echo "</pre>";
			//echo $output;
			$this->getFormToken();
			curl_close($this->ch);
			
			return array("status" => $status, "output" => $output);
		}
	}
	$qr = new QrReceiver("http://172.29.35.163/trac/".$_GET["project"]);
	echo "<pre>";
	print_r($qr->login());
	
	if(isset($_GET["resolve"])) {
		$r = $qr->loadTicket($_GET["id"]);
		$r = $qr->resolve($_GET["id"]);
		print_r($r);
	}
	echo "</pre>";
?>
