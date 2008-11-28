#
# Bob the builder - as brought to you by Michael Neale
# http://users.on.net/michaelneale
#
require "socket"
require "net/http"
require "cgi"


# The irc class, which talks to the server and holds the main event loop
class IRC
    def initialize(server, port, nick, channel)
        @server = server
        @port = port
        @nick = nick
        @channel = channel
    end
    def send(s)
        # Send a message to the irc server and print it to the screen
        puts "--> #{s}"
        @irc.send "#{s}\n", 0 
    end
    def connect()
        # Connect to the IRC server
        @irc = TCPSocket.open(@server, @port)
        send "USER blah blah blah :blah blah"
        send "NICK #{@nick}"
        send "JOIN #{@channel}"
    end

    def handle_server_input(s)
        # This isn't at all efficient, but it shows what we can do with Ruby
        # (Dave Thomas calls this construct "a multiway if on steroids")
	puts "The command is: " + s.strip
        case s.strip
            when /^PING :(.+)$/i
                puts "[ Server ping ]"
                send "PONG :#{$1}"
            when /^:(.+?)!(.+?)@(.+?)\sPRIVMSG\s.+\s:[\001]PING (.+)[\001]$/i
                puts "[ CTCP PING from #{$1}!#{$2}@#{$3} ]"
                send "NOTICE #{$1} :\001PING #{$4}\001"
            when /^:(.+?)!(.+?)@(.+?)\sPRIVMSG\s.+\s:[\001]VERSION[\001]$/i
                puts "[ CTCP VERSION from #{$1}!#{$2}@#{$3} ]"
                send "NOTICE #{$1} :\001VERSION Ruby-irc v0.042\001"
	    when /.*lisa:\s*build\s*.*/ 		
			reply "updating and building..."			
			result = `/bin/sh ./build.sh`
			if result =~ /.*BUILD SUCCESSFUL.*/ then
 				reply "BUILD SUCCESS. Thanks for asking !"
				@clean = true
		        else 
				reply "BUILD FAILURE. Time to do a checkout and try for youself !"
				@clean = false
			end
	    when /.*lisa:\s*hi.*/                
		reply "Hi. I am Mic's build mistress. Type 'lisa: build' to kick off a build. The last build status: " + @clean.to_s
                
        end
	puts s
    end

    def reply message 
	send "PRIVMSG #{@channel} : " + message
    end

    def no_paste message
	h = Net::HTTP.new("rafb.net", 80)
	resp, body = h.post("/paste/paste.php",
                    "lang=" + CGI.escape("Java") +
                    "&nick=" + CGI.escape("JBossRules") + 
		    "&desc=" + CGI.escape(message) + 
		    "&cvt_tabs=No"
                    )

	resp.each { |key, val| puts key + "==" +  val }
	return body
	
    end

    def main_loop()
        # Just keep on truckin' until we disconnect
        while true
            ready = select([@irc, $stdin], nil, nil, nil)
            next if !ready
            for s in ready[0]
                if s == $stdin then
                    return if $stdin.eof
                    s = $stdin.gets
                    send s
                elsif s == @irc then
                    return if @irc.eof
                    s = @irc.gets
                    handle_server_input(s)
                end
            end
        end
    end
end

# The main program
# If we get an exception, then print it out and keep going (we do NOT want
# to disconnect unexpectedly!)
irc = IRC.new('irc.codehaus.org', 6667, 'lisa', '#drools')
#irc = IRC.new('irc.freenode.net',6667, 'pilondial', '#jbms')
irc.connect()
begin
    irc.main_loop()
rescue Interrupt
rescue Exception => detail
    puts detail.message()
    print detail.backtrace.join("\n")
    retry
end
