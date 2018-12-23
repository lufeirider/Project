from scapy.all import *

dst_ip = "192.168.100.101"

for i in range(445,446):
    packet_id = random.randint(10000, 2**16-1)
    tcp_raw = str(IP(dst=dst_ip) / TCP(sport=RandShort(),dport=i,flags=0x002))[20:]

    data1 = tcp_raw[0:8]
    data2 = tcp_raw[8:16]
    data3 = tcp_raw[16:20]


    print(hexdump(tcp_raw))

    one_part_request = IP(dst=dst_ip,ttl=49,flags=0x01,proto=0x06,id=packet_id,frag=0)
    two_part_request = IP(dst=dst_ip,ttl=49,flags=0x01,proto=0x06,id=packet_id,frag=1)
    three_part_request = IP(dst=dst_ip,ttl=49,flags=0x00,proto=0x06,id=packet_id,frag=2)


    send(one_part_request / Raw(data1), verbose=False, return_packets=False)
    send(two_part_request / Raw(data2), verbose=False, return_packets=False)
    send(three_part_request / Raw(data3), verbose=False, return_packets=False)

