import { Home, Users, Settings, Package2 } from "lucide-react";

export const mainNavigation = [
  {
    name: "Dashboard",
    href: "/dashboard",
    icon: Home,
  },
  {
    name: "Users",
    href: "/dashboard/users",
    icon: Users,
  },
  {
    name: "Settings",
    href: "/dashboard/settings",
    icon: Settings,
  },
];

export const logoNavigation = {
  name: "Admin Dashboard",
  href: "/dashboard",
  icon: Package2,
};