# importing element tree
# under the alias of ET
import xml.etree.ElementTree as ET

filename = "test_AndroidManifest.xml"

# Passing the path of the
# xml document to enable the
# parsing process
tree = ET.parse(filename)

# getting the parent tag of
# the xml document
root = tree.getroot()

permissions_list = []

for permissions in root.iter('uses-permission'):
    # print(permissions.attrib['{http://schemas.android.com/apk/res/android}name'])
    permissions_list.append(permissions.attrib['{http://schemas.android.com/apk/res/android}name'])

# Remove duplicates from permissions_list
permissions_list = list(dict.fromkeys(permissions_list))
    
print(permissions_list)