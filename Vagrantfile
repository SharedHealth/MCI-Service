# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  # config.vm.box = "centos65"
  config.vm.box = "nrel/CentOS-6.5-x86_64"
  # config.vm.box_url = "http://puppet-vagrant-boxes.puppetlabs.com/centos-65-x64-virtualbox-puppet.box"
  config.vm.network "private_network", ip: "192.168.33.19"
  config.vm.provider "virtualbox" do |vb|
    vb.customize ["modifyvm", :id, "--memory", "2048", "--cpus", "2"]
    vb.customize ["modifyvm", :id, "--natdnshostresolver1", "on"]
    vb.customize ["modifyvm", :id, "--natdnsproxy1", "on"]
  end
  config.vm.host_name = "192.168.33.19"

  config.vm.define "192.168.33.19" do |mci|
      mci.vm.provision "ansible" do |ansible|
        ansible.inventory_path = "../FreeSHR-Playbooks/inventories/local"
        ansible.playbook =  "../FreeSHR-Playbooks/all.yml"
        ansible.tags = ["setup", "cassandra"]
	ansible.extra_vars= { java_runtime: "jdk", setup_nrpe: "no" }
        ansible.vault_password_file = "~/.vaultpass.txt"
        ansible.verbose = 'vvvv'
      end
      mci.vm.provision "ansible" do |ansible|
        ansible.inventory_path = "../FreeSHR-Playbooks/inventories/local"
        ansible.playbook =  "../FreeSHR-Playbooks/mci.yml"
        ansible.tags = ["mci-server"]
        ansible.extra_vars= { java_runtime: "jdk", setup_nrpe: "no" }
        ansible.vault_password_file = "~/.vaultpass.txt"
        ansible.verbose = 'vvvv'
      end
  end
end
