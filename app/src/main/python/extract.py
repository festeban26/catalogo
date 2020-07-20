import mammoth
import bs4
import re
import json
import os

if os.name == 'nt':
    import win32api
    import win32con

none_equivalent_characters = ['-', 'NA']


# Function to check if file is hidden
def file_is_hidden(p):
    if os.name == 'nt':
        attribute = win32api.GetFileAttributes(p)
        return attribute & (win32con.FILE_ATTRIBUTE_HIDDEN | win32con.FILE_ATTRIBUTE_SYSTEM)
    else:
        return p.startswith('.')  # linux-osx


def remove_img_tags(data):
    p = re.compile(r'<img.*?/>')
    return p.sub('', data)


def populate_product_general_information_dic_with_values_from_row(row, product_general_information):
    for element in row:
        element_text = element.get_text(" ", strip=True)
        if element_text.startswith("NOMBRE"):
            product_general_information['productName'] = element.nextSibling.get_text(" ", strip=True)
            return
        elif element_text.startswith("CONTENIDO"):
            product_general_information['netWeight'] = element.nextSibling.get_text(" ", strip=True)
            continue
        elif element_text.startswith("MARCA"):
            product_general_information['brand'] = element.nextSibling.get_text(" ", strip=True)
            return
        elif element_text == "INGREDIENTES":
            product_general_information['ingredients'] = element.nextSibling.get_text(" ", strip=True)
            return
        elif element_text.startswith("INGREDIENTES"):
            allergies_text = element.nextSibling.get_text(" ", strip=True).replace('“', '').replace('”', '')
            if allergies_text not in none_equivalent_characters:
                product_general_information['allergies'] = allergies_text
            return
        elif element_text.startswith("INSTRUCCIONES"):
            sub_elements = element.nextSibling.find_all('li')
            test_dict = []
            for sub_element in sub_elements:
                test_dict.append(sub_element.get_text(" ", strip=True))
                product_general_information['preservation'] = test_dict
            return
        elif element_text.startswith("CERTIFICADO"):
            product_general_information['sanitaryRegistry'] = element.nextSibling.get_text(" ", strip=True)
            return
        elif element_text.startswith("COMERCIALES"):
            sub_elements = element.nextSibling.find_all('p')
            for sub_element in sub_elements:
                if sub_element.get_text(" ", strip=True).startswith("Código"):
                    barcode = sub_element.get_text(" ", strip=True).split(':', 1)[1].strip()
                    if representsFloat(barcode):
                        product_general_information['barcode'] = barcode
                elif sub_element.get_text(" ", strip=True).startswith("Recomend"):
                    product_general_information['commercialWarning'] = sub_element.get_text(" ", strip=True).split(':', 1)[1].strip()
            return
        else:
            return


def representsFloat(s):
    try:
        float(s)
        return True
    except ValueError:
        return False


def get_nutritional_info_tuple_data(sibling):
    dic = {}
    data_value_text = sibling.get_text(" ", strip=True).replace(" ", "")
    data_percentage_text = sibling.nextSibling.get_text(" ", strip=True).replace(" ", "")

    if data_value_text:
        if data_value_text not in none_equivalent_characters:
            dic['value'] = data_value_text

    if data_percentage_text:
        if data_percentage_text not in none_equivalent_characters:
            dic['percentage'] = data_percentage_text

    return dic


def populate_nutritional_information_dic_with_values_from_row(row, dic):
    for element in row:
        element_text = element.get_text(" ", strip=True).lower()
        if element_text.startswith("tamaño"):
            dic['amountPerServing'] = element.nextSibling.get_text(" ", strip=True)
            return
        elif element_text.startswith("porciones"):
            dic['servingsPerContainer'] = element.nextSibling.get_text(" ", strip=True)
            return
        elif element_text.startswith("energía ("):
            dic['valueCalories'] = element.get_text(" ", strip=True).split(':', 1)[1].strip()
            return
        elif element_text.startswith("energía d"):
            dic['valueFatCalories'] = element.get_text(" ", strip=True).split(':', 1)[1].strip()
            return
        elif element_text.startswith("grasa total"):
            dic['totalFat'] = get_nutritional_info_tuple_data(element.nextSibling)
            return
        elif element_text.startswith("grasa saturada"):
            dic['satFat'] = get_nutritional_info_tuple_data(element.nextSibling)
            return
        elif element_text.startswith("grasas trans"):
            dic['transFat'] = get_nutritional_info_tuple_data(element.nextSibling)
            return
        elif element_text.startswith("grasas mono") or element_text.startswith("grasa mono"):
            dic['monoUnsaturatedFat'] = get_nutritional_info_tuple_data(element.nextSibling)
            return
        elif element_text.startswith("grasas poli") or element_text.startswith("grasa poli"):
            dic['polyUnsaturatedFat'] = get_nutritional_info_tuple_data(element.nextSibling)
            return
        elif element_text.startswith("colesterol"):
            dic['cholesterol'] = get_nutritional_info_tuple_data(element.nextSibling)
            return
        elif element_text.startswith("sodio"):
            dic['sodium'] = get_nutritional_info_tuple_data(element.nextSibling)
            return
        elif element_text.startswith("carbo"):
            dic['totalCarb'] = get_nutritional_info_tuple_data(element.nextSibling)
            return
        elif element_text.startswith("fibra"):
            dic['fibers'] = get_nutritional_info_tuple_data(element.nextSibling)
            return
        elif element_text.startswith("azúcares"):
            dic['sugars'] = get_nutritional_info_tuple_data(element.nextSibling)
            return
        elif element_text.startswith("proteína"):
            dic['proteins'] = get_nutritional_info_tuple_data(element.nextSibling)
            return
        else:
            return


def populate_vitamins_dic(row, vitamins_information_dic):
    for element in row:
        element_text = element.get_text(" ", strip=True).lower()
        if element_text.startswith("vitamina c"):
            vitamins_information_dic['vitaminC'] = get_nutritional_info_tuple_data(element.nextSibling)
            return
        elif element_text.startswith("vitamina b2"):
            vitamins_information_dic['vitaminB2'] = get_nutritional_info_tuple_data(element.nextSibling)
            return
        elif element_text.startswith("vitamina b3"):
            vitamins_information_dic['vitaminB3'] = get_nutritional_info_tuple_data(element.nextSibling)
            return
        elif element_text.startswith("vitamina b6"):
            vitamins_information_dic['vitaminB6'] = get_nutritional_info_tuple_data(element.nextSibling)
            return
        else:
            return


def get_dic_with_vitamins_information(rows):
    vitamins_information_dic = {}
    for row in rows:
        populate_vitamins_dic(row, vitamins_information_dic)
    return vitamins_information_dic


documents_directory = os.path.join(os.getcwd(), "Documents")
# traverse Documents directory, and list files in it
for root, dirs, files in os.walk(documents_directory):
    products_list = []
    for file in files:
        file_path = os.path.join(documents_directory, file)
        # only consider docx files. Ignore hidden files
        if not file_is_hidden(file_path) and file.endswith(".docx"):
            print("Processing file '" + file_path + "'")
            with open(file_path, 'rb') as docx_file:
                html = mammoth.convert_to_html(docx_file).value  # The generated HTML
                soup = bs4.BeautifulSoup(html, 'html.parser')
                # remove images
                soup = bs4.BeautifulSoup(remove_img_tags(str(soup)), 'html.parser')

                current_product_dic = {}

                tables = soup.find_all('table')
                main_table = tables[0]
                nutrition_table = tables[1]

                #  process the nutrition table rows
                nutrition_table_rows = nutrition_table.find_all('tr')
                current_product_nutritional_information_dic = {}
                vitamins_rows = []

                for tr in nutrition_table_rows:
                    td = tr.find_all('td')
                    if td:
                        row = [i for i in td]
                        row_text = [i.text for i in td]
                        if row_text[0].startswith('Vitamina'):
                            vitamins_rows.append(row)
                        populate_nutritional_information_dic_with_values_from_row(
                            row, current_product_nutritional_information_dic)

                # populate nutritional information dictionary with vitamins information
                current_product_nutritional_information_dic['vitamins'] \
                    = get_dic_with_vitamins_information(vitamins_rows)

                current_product_dic["nutritionalInformation"] = current_product_nutritional_information_dic

                #  after extracting the nutrition table info, remove internal tables of main table
                for table in main_table.find_all('table'):
                    table.decompose()

                #  get the main table rows
                main_table_rows = tables[0].find_all('tr')
                for tr in main_table_rows:
                    td = tr.find_all('td')
                    if td:
                        row = [i for i in td]
                        populate_product_general_information_dic_with_values_from_row(row, current_product_dic)

                products_list.append(current_product_dic)
                docx_file.close()

    products_dic = {"products": products_list, "version": "1.0"}

with open('products_data.json', 'w') as outfile:
    json.dump(products_dic, outfile, ensure_ascii=True, sort_keys=True)
