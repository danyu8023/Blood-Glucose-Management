echo '===== iptables full ====='
iptables -S
echo
iptables -L -n -v --line-numbers
echo
iptables -L YJ-FIREWALL-INPUT -n -v --line-numbers 2>/dev/null
echo
echo '===== nftables ====='
nft list ruleset 2>/dev/null