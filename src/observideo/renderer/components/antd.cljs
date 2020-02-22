(ns observideo.renderer.components.antd
  (:require
   [reagent.core :as reagent]
   ["antd" :refer [Layout Menu Breadcrumb Icon Button PageHeader Table Breadcrumb]]))

(def layout (reagent/adapt-react-class Layout))
(def header (reagent/adapt-react-class (.-Header Layout)))
(def content (reagent/adapt-react-class (.-Content Layout)))
(def sider (reagent/adapt-react-class (.-Sider Layout)))
(def footer (reagent/adapt-react-class (.-Footer Layout)))
(def menu (reagent/adapt-react-class Menu))
(def submenu (reagent/adapt-react-class (.-SubMenu Menu)))
(def menuitem (reagent/adapt-react-class (.-Item Menu)))
(def icon (reagent/adapt-react-class Icon))
(def page-header (reagent/adapt-react-class PageHeader))
(def button (reagent/adapt-react-class Button))
(def table (reagent/adapt-react-class Table))
(def columngroup (reagent/adapt-react-class (.-ColumnGroup Table)))
(def column (reagent/adapt-react-class (.-Column Table)))
(def breadcrumb (reagent/adapt-react-class Breadcrumb))
(def breadcrumb-item (reagent/adapt-react-class (aget Breadcrumb "Item")))
(def breadcrumb-sep (reagent/adapt-react-class (aget Breadcrumb "Separator")))


