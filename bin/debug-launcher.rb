#!/usr/bin/ruby

require 'fileutils'
require 'optparse'

class Launcher

  include FileUtils

  BASEDIR=File.dirname(File.dirname(File.expand_path(__FILE__)))

  def do_exec( cmd )
    puts "Running: '#{cmd}'..."
    system( cmd )
    
    result=$?
    if ( result != 0 )
      puts "#{cmd} exited with '#{result}'"
      exit result
    end
  end

  def run(args)

    config={
      :debug => true,
      :clean => true,
      :launch => true,
      :uidev => false,
    }

    OptionParser.new{|opts|
      opts.on('-d', '--debug', 'setup java debug port 8000'){config[:debug] = true}
      opts.on('-e', '--existing', 'keep existing unpacked directory if it exists'){config[:clean]=false}
      opts.on('-L', '--nolaunch', 'do not launch!'){config[:launch]=false}
      opts.on('-u', '--uidev', 'Link to the sources for the UI to enable live UI development' ){config[:uidev]=true}

      config[:args] = opts.parse!(args)
    }

    puts "Launching swapmeat..."

    target = File.join( BASEDIR, 'target' )
    launch_dir = File.join(target, "swapmeat" )

    rm_rf( launch_dir ) if config[:clean]

    glob = File.join( target, "swapmeat-*-dist.tar.gz" )

    puts "Looking for launcher archives: '#{glob}"
    archives = Dir.glob(glob)

    puts "Found matching archives: #{archives}"
    do_exec( "tar -zxvf #{archives[0]} -C #{target}" )

    if (config[:uidev])
      mv( "#{launch_dir}/ui", "#{launch_dir}/ui.bak" )
      ln_s( "#{BASEDIR}/src/main/js/app", "#{launch_dir}/ui" )
    end

    if ( config[:debug] )
      env_sh = "#{launch_dir}/etc/swapmeat/env.sh"

      lines = []
      lines = File.readlines(env_sh) if ( File.exists?(env_sh))
      debug_line = 'export JAVA_DEBUG_OPTS="-Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"'

      found = false
      lines.each{|line|
        if ( line =~ /^export JAVA_DEBUG_OPTS.+/ )
          found = true
          break
        end
      }

      lines << debug_line unless found
      File.open(env_sh, 'w+'){|f|
        lines.each{|line|
          f.puts(line)
        }
      }
    end

    if ( config[:launch] )
      exec( "#{launch_dir}/bin/swapmeat.sh #{config[:args].join(' ')}" )
    else
      exec("cd #{launch_dir}")
    end
  end
end

Launcher.new().run(ARGV)
