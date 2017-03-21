Vagrant.configure("2") do |config|

  config.vm.box = "ubuntu/trusty64"
  config.vm.network "forwarded_port", guest: 22, host: 2222, id: "ssh", disabled: true
  config.ssh.shell = "bash -c 'BASH_ENV=/etc/profile exec bash'"

  config.vm.define :titan do |titan|
    titan.vm.hostname="titan"
    titan.vm.network :private_network, ip: "192.168.58.100"
    titan.vm.network "forwarded_port", guest: 22, host: 5022

    # Gremlin server port
    titan.vm.network "forwarded_port", guest: 8182, host: 8182

    # Spark master/slave ports
    titan.vm.network "forwarded_port", guest: 9080, host: 9080
    titan.vm.network "forwarded_port", guest: 9081, host: 9081

    titan.vm.provider :virtualbox do |vb|
	    vb.name = "titan"
      vb.memory = 8192
      vb.cpus = 2
    end

    titan.vm.provision "shell", path: "provision.sh"
  end

end
