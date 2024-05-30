import urllib.request
import hashlib
import UnityPy
import json

def main():
    hash = get_version_hash()
    data = get_asset_data(hash)
    catalog = extract_catalog(data)

    # Save catalog to a file
    # with open("catalog.json", "w") as catalog_file:
    #    json.dump(catalog, catalog_file, indent=4)

    urls = get_urls(hash, catalog)

    # Save URLs to a file
    with open("urls.json", "w") as urls_file:
        json.dump(urls, urls_file, indent=4)
    
    asset_bundles = filter_asset_bundles(catalog)
    
    # Save filtered asset bundles to a file
    with open("AssetBundles.json", "w") as asset_bundles_file:
        json.dump(asset_bundles, asset_bundles_file, indent=4)

def extract_catalog(asset):
    env = UnityPy.load(asset)
    for obj in env.objects:
        if obj.type.name == "MonoBehaviour":
            tree = obj.read_typetree()
            return tree

def get_asset_data(versionHash):
    path = "http://nah3ohdioj7ye3sh.dblgnds.channel.or.jp/clientdata/" + versionHash + "/a/"
    path += "e4bbe5b7a4c1eb55652965aee885dd59bd2ee7f4"
    langresponse = urllib.request.urlopen(path)
    return langresponse.read()

def get_version_hash():
    response = urllib.request.urlopen("http://nah3ohdioj7ye3sh.dblgnds.channel.or.jp/clientdata/index")
    assetVersion = response.read()[0:8]
    versionHashString = hashlib.sha1(bytes.fromhex(assetVersion.decode("utf-8")) + bytes("asset", "utf-8"))
    versionHash = versionHashString.hexdigest()
    return versionHash

def get_urls(versionHash, catalog):
    url_json = {}
    for i, v in enumerate(catalog["m_AssetBundleNames"]):                                                                          
        if "streamingassetbundles" in v:
            url_json[v] = "http://nah3ohdioj7ye3sh.dblgnds.channel.or.jp/clientdata/" + versionHash + "/a/" + hashlib.sha1(bytes(v, "utf-8")).hexdigest()
    return url_json

def filter_asset_bundles(catalog):
    filtered_bundles = []
    for v in catalog["m_AssetBundleNames"]:
        if v.startswith("character/data/battle"):
            if v[len("character/data/battle/")].isdigit():
                filtered_bundles.append(v)
            else:
                return filtered_bundles
    return filtered_bundles

if __name__ == "__main__":
    main()
