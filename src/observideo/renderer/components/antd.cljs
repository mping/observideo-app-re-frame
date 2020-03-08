(ns observideo.renderer.components.antd
  (:require [reagent.core :as reagent]
            ["antd" :refer [Layout Menu Breadcrumb Icon Button PageHeader Table Breadcrumb
                            Row Col
                            Form Input Slider]]
            ["@ant-design/icons" :refer [VideoCameraOutlined TagsOutlined BarChartOutlined
                                         UploadOutlined EditOutlined DeleteOutlined SaveOutlined
                                         PlusCircleOutlined MinusCircleOutlined MinusOutlined]]))

(def videos-icon (.-render VideoCameraOutlined))
(def templates-icon (.-render TagsOutlined))
(def queries-icon (.-render BarChartOutlined))

(def upload-icon (.-render UploadOutlined))
(def edit-icon (.-render EditOutlined))
(def delete-icon (.-render DeleteOutlined))
(def save-icon (.-render SaveOutlined))

(def plus-circle-icon (.-render PlusCircleOutlined))
(def minus-circle-icon (.-render MinusCircleOutlined))
(def minus-icon (.-render MinusOutlined))

(def layout (reagent/adapt-react-class Layout))
(def row (reagent/adapt-react-class Row))
(def col (reagent/adapt-react-class Col))
(def header (reagent/adapt-react-class (.-Header Layout)))
(def content (reagent/adapt-react-class (.-Content Layout)))
(def sider (reagent/adapt-react-class (.-Sider Layout)))
(def footer (reagent/adapt-react-class (.-Footer Layout)))
(def menu (reagent/adapt-react-class Menu))
(def submenu (reagent/adapt-react-class (.-SubMenu Menu)))
(def menuitem (reagent/adapt-react-class (.-Item Menu)))

(def page-header (reagent/adapt-react-class PageHeader))

(def button (reagent/adapt-react-class Button))
(def table (reagent/adapt-react-class Table))
(def columngroup (reagent/adapt-react-class (.-ColumnGroup Table)))
(def column (reagent/adapt-react-class (.-Column Table)))

(def form (reagent/adapt-react-class Form))
(def form-item (reagent/adapt-react-class (aget Form "Item")))
(def input (reagent/adapt-react-class Input))
(def slider (reagent/adapt-react-class Slider))

(def breadcrumb (reagent/adapt-react-class Breadcrumb))
(def breadcrumb-item (reagent/adapt-react-class (aget Breadcrumb "Item")))
(def breadcrumb-sep (reagent/adapt-react-class (aget Breadcrumb "Separator")))


