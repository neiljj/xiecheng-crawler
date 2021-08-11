# import sys
# print(sys.path)
import execjs
# print(execjs.__file__)

# fmd5 = '3e1ef1c42b1be7f422fd1425e8e1f165'
# with open('/Users/qudian/Downloads/g_encrypt.js', 'r') as f:
#     ctx1 = execjs.compile(f.read(), cwd=r'/Users/qudian/node_modules')
# encrypt_str = ctx1.call('b', fmd5)
# print(fmd5)
# print(encrypt_str)


# import execjs


def encoder(fmd5):
    # fmd5 = '3e1ef1c42b1be7f422fd1425e8e1f165'
    with open('/Users/qudian/Downloads/g_encrypt.js', 'r') as f:
        ctx1 = execjs.compile(f.read(), cwd=r'/Users/qudian/node_modules')
    encrypt_str = ctx1.call('b', fmd5)
    # print(fmd5)
    # print(encrypt_str)
    return encrypt_str